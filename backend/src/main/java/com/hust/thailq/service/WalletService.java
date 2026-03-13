package com.hust.thailq.service;

import com.hust.thailq.config.MessageSourceConfig;
import com.hust.thailq.domain.entity.User;
import com.hust.thailq.domain.entity.UserSession;
import com.hust.thailq.domain.entity.Wallet;
import com.hust.thailq.domain.entity.Transaction;
import com.hust.thailq.domain.enums.Status;
import com.hust.thailq.domain.enums.WalletStatus;
import com.hust.thailq.domain.enums.RoleType;
import com.hust.thailq.dto.mapper.WalletRequestMapper;
import com.hust.thailq.dto.mapper.WalletResponseMapper;
import com.hust.thailq.dto.mapper.WalletTransactionRequestMapper;
import com.hust.thailq.dto.request.TransactionRequest;
import com.hust.thailq.dto.request.WalletRequest;
import com.hust.thailq.dto.response.CommandResponse;
import com.hust.thailq.dto.response.UserResponse;
import com.hust.thailq.dto.response.WalletResponse;
import com.hust.thailq.exception.ElementAlreadyExistsException;
import com.hust.thailq.exception.InsufficientFundsException;
import com.hust.thailq.exception.NoSuchElementFoundException;
import com.hust.thailq.repository.UserRepository;
import com.hust.thailq.repository.UserSessionRepository;
import com.hust.thailq.repository.WalletRepository;
import com.hust.thailq.security.UserDetailsImpl;
import com.hust.thailq.validator.IbanValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.util.List;

import static com.hust.thailq.common.MessageKeys.*;

/**
 * Service used for Wallet related operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final MessageSourceConfig messageConfig;
    private final WalletRepository walletRepository;
    private final TransactionService transactionService;
    private final WalletRequestMapper walletRequestMapper;
    private final WalletResponseMapper walletResponseMapper;
    private final WalletTransactionRequestMapper walletTransactionRequestMapper;
    private final IbanValidator ibanValidator;
    private final UserTrackingService userTrackingService;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final FraudDetectionService fraudDetectionService;
    private final IbanAnalysisService ibanAnalysisService;

    /**
     * Fetches a single wallet by the given id.
     *
     * @param id
     * @return WalletResponse
     */
    @Transactional(readOnly = true)
    public WalletResponse findById(long id) {
        return walletRepository.findById(id)
                .map(this::mapWalletToDto)
                .orElseThrow(() -> new NoSuchElementFoundException(messageConfig.getMessage(ERROR_WALLET_NOT_FOUND)));
    }

    /**
     * Fetches a single wallet by the given iban.
     *
     * @param iban
     * @return WalletResponse
     */
    @Transactional(readOnly = true)
    public WalletResponse findByIban(String iban) {
        return walletRepository.findByIban(iban)
                .map(this::mapWalletToDto)
                .orElseThrow(() -> new NoSuchElementFoundException(messageConfig.getMessage(ERROR_WALLET_NOT_FOUND)));
    }

    /**
     * Fetches a single wallet by the given userId.
     *
     * @param userId
     * @return WalletResponse
     */
    @Transactional(readOnly = true)
    public List<WalletResponse> findByUserId(long userId) {
        List<Wallet> wallets = walletRepository.findByUserIdWithUser(userId);
        log.info("[DEBUG] Wallets for user {}: count = {}", userId, wallets.size());
        for (Wallet w : wallets) {
            log.info("[DEBUG] Wallet: id={}, name={}, status={}, user_id={}", w.getId(), w.getName(), w.getStatus(), w.getUser().getId());
        }
        return wallets.stream()
                .map(this::mapWalletToDto)
                .toList();
    }

    /**
     * Fetches a single wallet reference (entity) by the given id.
     *
     * @param iban
     * @return Wallet
     */
    public Wallet getByIban(String iban) {
        return walletRepository.findByIban(iban)
                .orElseThrow(() -> new NoSuchElementFoundException(messageConfig.getMessage(ERROR_WALLET_NOT_FOUND)));
    }

    /**
     * Fetches all wallets based on the given paging and sorting parameters.
     *
     * @param pageable
     * @return List of WalletResponse
     */
    @Transactional(readOnly = true)
    public Page<WalletResponse> findAll(Pageable pageable) {
        final Page<Wallet> wallets = walletRepository.findAll(pageable);
        if (wallets.isEmpty())
            throw new NoSuchElementFoundException(messageConfig.getMessage(ERROR_NO_RECORDS));
        
        // Use mapWalletToDto method to include IBAN analysis
        Page<WalletResponse> response = wallets.map(this::mapWalletToDto);
        
        return response;
    }

    /**
     * Creates a new wallet using the given request parameters.
     *
     * @param request
     * @return id of the created wallet
     */
    @Transactional
    public CommandResponse create(WalletRequest request) {
        if (walletRepository.existsByIbanIgnoreCase(request.getIban()))
            throw new ElementAlreadyExistsException(messageConfig.getMessage(ERROR_WALLET_IBAN_EXISTS));
        if (walletRepository.existsByUserIdAndNameIgnoreCase(request.getUserId(), request.getName()))
            throw new ElementAlreadyExistsException(messageConfig.getMessage(ERROR_WALLET_NAME_EXISTS));

        ibanValidator.isValid(request.getIban(), null);

        final Wallet wallet = walletRequestMapper.toEntity(request);
        wallet.setStatus(WalletStatus.ACTIVE);
        walletRepository.save(wallet);

        // add this initial amount to the transactions
        transactionService.create(walletTransactionRequestMapper.toTransactionDto(request));

        return CommandResponse.builder().id(wallet.getId()).build();
    }

    /**
     * Transfer funds between wallets.
     *
     * @param request
     * @return id of the transaction
     */
    @Transactional
    public CommandResponse transferFunds(TransactionRequest request) {
        UserSession session = null;
        User user = null;
        try {
            // Get current user
            user = getCurrentUser();
            if (user != null) {
                // Check for location changes and update session if needed
                session = userTrackingService.checkAndUpdateSessionForLocationChange(user);
            }

            // Fraud detection - MUST happen BEFORE processing the transaction
            if (user != null && session != null) {
                // Build a Transaction entity for fraud checking (but do not save yet)
                Transaction txForCheck = transactionService.getTransactionRequestMapper().toEntity(request);
                // Set fromWallet, toWallet, type (mapper does this in @AfterMapping)
                // Run rule-based fraud detection
                List<String> fraudReasons = fraudDetectionService.checkTransactionFraud(txForCheck, user, session);
                if (!fraudReasons.isEmpty()) {
                    log.warn("FRAUDULENT TRANSACTION BLOCKED for user {}: {}", user.getId(), String.join("; ", fraudReasons));
                    // Track the blocked fraudulent transaction
                    userTrackingService.trackTransferActivity(user, request.getAmount(), 
                        request.getFromWalletIban(), request.getToWalletIban(), session, false, String.join("; ", fraudReasons));
                    return CommandResponse.builder().id(null).message("Fraudulent transaction detected: " + String.join("; ", fraudReasons)).build();
                }
            }

            final Wallet toWallet = getByIban(request.getToWalletIban());
            final Wallet fromWallet = getByIban(request.getFromWalletIban());

            // Prevent transactions with wallets that are not ACTIVE
            if (fromWallet.getStatus() != WalletStatus.ACTIVE) {
                log.warn("Attempted transfer from wallet '{}' (ID: {}) with status {}. Transaction blocked.", fromWallet.getName(), fromWallet.getId(), fromWallet.getStatus());
                throw new IllegalStateException("Ví điện tử nguồn đang bị đóng. Không thể thực hiện giao dịch.");
            }
            if (toWallet.getStatus() != WalletStatus.ACTIVE) {
                log.warn("Attempted transfer to wallet '{}' (ID: {}) with status {}. Transaction blocked.", toWallet.getName(), toWallet.getId(), toWallet.getStatus());
                throw new IllegalStateException("Ví điện tử nhận đang bị đóng. Không thể thực hiện giao dịch.");
            }

            // check if the balance of sender wallet has equal or higher to/than transfer amount
            if (fromWallet.getBalance().compareTo(request.getAmount()) < 0) {
                if (user != null && session != null) {
                    userTrackingService.trackTransferActivity(user, request.getAmount(), 
                        request.getFromWalletIban(), request.getToWalletIban(), session, false, 
                        "Insufficient funds");
                }
                throw new InsufficientFundsException(messageConfig.getMessage(ERROR_INSUFFICIENT_FUNDS));
            }

            // update balance of the sender wallet
            fromWallet.setBalance(fromWallet.getBalance().subtract(request.getAmount()));

            // update balance of the receiver wallet
            toWallet.setBalance(toWallet.getBalance().add(request.getAmount()));

            walletRepository.save(toWallet);
            log.info(messageConfig.getMessage(INFO_WALLET_BALANCES_UPDATED, fromWallet.getBalance(), toWallet.getBalance()));

            // Create transaction and get entity
            Transaction transaction = transactionService.createAndReturnEntity(request);

            // Track successful transfer
            if (user != null && session != null) {
                userTrackingService.trackTransferActivity(user, request.getAmount(), 
                    request.getFromWalletIban(), request.getToWalletIban(), session, true, null);
            }

            return CommandResponse.builder().id(transaction.getId()).build();
        } catch (Exception e) {
            // Track failed transfer
            if (user != null && session != null) {
                userTrackingService.trackTransferActivity(user, request.getAmount(), 
                    request.getFromWalletIban(), request.getToWalletIban(), session, false, e.getMessage());
            }
            throw e;
        }
    }

    /**
     * Adds funds to the given wallet.
     *
     * @param request
     * @return id of the transaction
     */
    @Transactional
    public CommandResponse addFunds(TransactionRequest request) {
        UserSession session = null;
        User user = null;
        try {
            // Get current user
            user = getCurrentUser();
            if (user != null) {
                // Check for location changes and update session if needed
                session = userTrackingService.checkAndUpdateSessionForLocationChange(user);
            }

            // Fraud detection - MUST happen BEFORE processing the transaction
            if (user != null && session != null) {
                Transaction txForCheck = transactionService.getTransactionRequestMapper().toEntity(request);
                List<String> fraudReasons = fraudDetectionService.checkTransactionFraud(txForCheck, user, session);
                if (!fraudReasons.isEmpty()) {
                    log.warn("FRAUDULENT ADD FUNDS BLOCKED for user {}: {}", user.getId(), String.join("; ", fraudReasons));
                    userTrackingService.trackAddFundsActivity(user, request.getAmount(), 
                        request.getToWalletIban(), session, false, String.join("; ", fraudReasons));
                    return CommandResponse.builder().id(null).message("Fraudulent transaction detected: " + String.join("; ", fraudReasons)).build();
                }
            }

            final Wallet toWallet = getByIban(request.getToWalletIban());

            // Prevent add funds to wallets that are not ACTIVE
            if (toWallet.getStatus() != WalletStatus.ACTIVE) {
                log.warn("Attempted add funds to wallet '{}' (ID: {}) with status {}. Transaction blocked.", toWallet.getName(), toWallet.getId(), toWallet.getStatus());
                throw new IllegalStateException("Ví điện tử đang bị đóng. Không thể nạp tiền.");
            }

            // update balance of the receiver wallet
            toWallet.setBalance(toWallet.getBalance().add(request.getAmount()));

            walletRepository.save(toWallet);
            log.info(messageConfig.getMessage(INFO_WALLET_BALANCE_UPDATED, toWallet.getBalance()));

            final CommandResponse response = transactionService.create(request);
            
            // Track successful add funds
            if (user != null && session != null) {
                userTrackingService.trackAddFundsActivity(user, request.getAmount(), 
                    request.getToWalletIban(), session, true, null);
            }
            
            return CommandResponse.builder().id(response.getId()).build();
        } catch (Exception e) {
            // Track failed add funds
            if (user != null && session != null) {
                userTrackingService.trackAddFundsActivity(user, request.getAmount(), 
                    request.getToWalletIban(), session, false, e.getMessage());
            }
            throw e;
        }
    }

    /**
     * Withdraw funds from the given wallet.
     *
     * @param request
     * @return id of the transaction
     */
    @Transactional
    public CommandResponse withdrawFunds(TransactionRequest request) {
        UserSession session = null;
        User user = null;
        try {
            // Get current user
            user = getCurrentUser();
            if (user != null) {
                // Check for location changes and update session if needed
                session = userTrackingService.checkAndUpdateSessionForLocationChange(user);
            }

            // Fraud detection - MUST happen BEFORE processing the transaction
            if (user != null && session != null) {
                Transaction txForCheck = transactionService.getTransactionRequestMapper().toEntity(request);
                List<String> fraudReasons = fraudDetectionService.checkTransactionFraud(txForCheck, user, session);
                if (!fraudReasons.isEmpty()) {
                    log.warn("FRAUDULENT WITHDRAW BLOCKED for user {}: {}", user.getId(), String.join("; ", fraudReasons));
                    userTrackingService.trackWithdrawActivity(user, request.getAmount(), 
                        request.getFromWalletIban(), session, false, String.join("; ", fraudReasons));
                    return CommandResponse.builder().id(null).message("Fraudulent transaction detected: " + String.join("; ", fraudReasons)).build();
                }
            }

            final Wallet fromWallet = getByIban(request.getFromWalletIban());

            // Prevent withdraw from wallets that are not ACTIVE
            if (fromWallet.getStatus() != WalletStatus.ACTIVE) {
                log.warn("Attempted withdraw from wallet '{}' (ID: {}) with status {}. Transaction blocked.", fromWallet.getName(), fromWallet.getId(), fromWallet.getStatus());
                throw new IllegalStateException("Ví điện tử đang bị đóng. Không thể rút tiền.");
            }

            // check if the balance of sender wallet has equal or higher to/than transfer amount
            if (fromWallet.getBalance().compareTo(request.getAmount()) < 0) {
                if (user != null && session != null) {
                    userTrackingService.trackWithdrawActivity(user, request.getAmount(), 
                        request.getFromWalletIban(), session, false, "Insufficient funds");
                }
                throw new InsufficientFundsException(messageConfig.getMessage(ERROR_INSUFFICIENT_FUNDS));
            }

            // update balance of the sender wallet
            fromWallet.setBalance(fromWallet.getBalance().subtract(request.getAmount()));

            walletRepository.save(fromWallet);
            log.info(messageConfig.getMessage(INFO_WALLET_BALANCE_UPDATED, fromWallet.getBalance()));

            final CommandResponse response = transactionService.create(request);
            
            // Track successful withdrawal
            if (user != null && session != null) {
                userTrackingService.trackWithdrawActivity(user, request.getAmount(), 
                    request.getFromWalletIban(), session, true, null);
            }
            
            return CommandResponse.builder().id(response.getId()).build();
        } catch (Exception e) {
            // Track failed withdrawal
            if (user != null && session != null) {
                userTrackingService.trackWithdrawActivity(user, request.getAmount(), 
                    request.getFromWalletIban(), session, false, e.getMessage());
            }
            throw e;
        }
    }

    /**
     * Updates wallet using the given request parameters.
     *
     * @param request
     * @return id of the updated wallet
     */
    public CommandResponse update(long id, WalletRequest request) {
        final Wallet foundWallet = walletRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementFoundException(messageConfig.getMessage(ERROR_WALLET_NOT_FOUND)));

        // check if the iban is changed and new iban is already exists
        if (!request.getIban().equalsIgnoreCase(foundWallet.getIban()) &&
                walletRepository.existsByIbanIgnoreCase(request.getIban()))
            throw new ElementAlreadyExistsException(messageConfig.getMessage(ERROR_WALLET_IBAN_EXISTS));

        // check if the name is changed and new name is already exists in user's wallets
        if (!request.getName().equalsIgnoreCase(foundWallet.getName()) &&
                walletRepository.existsByUserIdAndNameIgnoreCase(request.getUserId(), request.getName()))
            throw new ElementAlreadyExistsException(messageConfig.getMessage(ERROR_WALLET_NAME_EXISTS));

        ibanValidator.isValid(request.getIban(), null);

        final Wallet wallet = walletRequestMapper.toEntity(request);
        walletRepository.save(wallet);
        log.info(messageConfig.getMessage(INFO_WALLET_UPDATED, wallet.getIban(), wallet.getName(), wallet.getBalance()));
        return CommandResponse.builder().id(id).build();
    }

    /**
     * Deletes wallet by the given id.
     *
     * @param id
     */
    public void deleteById(long id) {
        final Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementFoundException(messageConfig.getMessage(ERROR_WALLET_NOT_FOUND)));
        walletRepository.delete(wallet);
        log.info(messageConfig.getMessage(INFO_WALLET_DELETED, wallet.getIban(), wallet.getName(), wallet.getBalance()));
    }

    @Transactional
    public void updateStatus(long walletId, WalletStatus newStatus) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            log.warn("Attempt to update wallet status by unauthenticated user.");
            throw new AccessDeniedException("User is not authenticated.");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new NoSuchElementFoundException("Wallet not found with id: " + walletId));

        WalletStatus oldStatus = wallet.getStatus();

        log.info("User '{}' attempting to change status for wallet '{}' (ID: {}). Current status: {}, New status: {}",
                currentUser.getUsername(), wallet.getName(), wallet.getId(), oldStatus, newStatus);

        boolean isOwner = wallet.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles().stream()
            .anyMatch(role -> role.getType() == RoleType.ROLE_ADMIN);

        if (!isOwner && !isAdmin) {
            log.error("SECURITY ALERT: User '{}' (ID: {}) attempted to change status of wallet (ID: {}) belonging to another user (ID: {}).",
                    currentUser.getUsername(), currentUser.getId(), wallet.getId(), wallet.getUser().getId());
            throw new AccessDeniedException("User is not authorized to update this wallet.");
        }

        if (oldStatus == newStatus) {
            log.info("No status change needed for wallet '{}'. It is already {}.", wallet.getName(), newStatus);
            return; // No operation needed
        }

        wallet.setStatus(newStatus);
        walletRepository.save(wallet);
        log.info("Successfully updated status of wallet '{}' (ID: {}) from {} to {}", wallet.getName(), wallet.getId(), oldStatus, newStatus);
    }

    // Helper methods to get current user and session
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userRepository.findById(userDetails.getId())
                    .orElse(null);
        }
        return null;
    }

    private UserSession getCurrentUserSession() {
        User user = getCurrentUser();
        if (user != null) {
            // Get the current active session for the user
            return userTrackingService.getCurrentActiveSession(user.getId());
        }
        return null;
    }

    public IbanAnalysisService getIbanAnalysisService() {
        return ibanAnalysisService;
    }

    private WalletResponse mapWalletToDto(Wallet wallet) {
        WalletResponse dto = new WalletResponse();
        dto.setId(wallet.getId());
        dto.setIban(wallet.getIban());
        dto.setName(wallet.getName());
        dto.setBalance(wallet.getBalance());
        
        // Format createdAt
        LocalDateTime datetime = LocalDateTime.ofInstant(wallet.getCreatedAt(), ZoneOffset.UTC);
        dto.setCreatedAt(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(datetime));
        
        // Map user data
        if (wallet.getUser() != null) {
            UserResponse userResponse = new UserResponse();
            userResponse.setId(wallet.getUser().getId());
            userResponse.setFirstName(wallet.getUser().getFirstName());
            userResponse.setLastName(wallet.getUser().getLastName());
            userResponse.setUsername(wallet.getUser().getUsername());
            userResponse.setEmail(wallet.getUser().getEmail());
            dto.setUser(userResponse);
        }
        
        // Analyze IBAN and get bank information
        IbanAnalysisService.IbanAnalysisResult bankInfo = ibanAnalysisService.analyzeIban(wallet.getIban());
        dto.setBankInfo(bankInfo.getBankName() + " (" + bankInfo.getCountryName() + ")");
        dto.setStatus(wallet.getStatus());
        
        return dto;
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> findByUsername(String username) {
        List<Wallet> wallets = walletRepository.findByUserUsernameIgnoreCase(username);
        return wallets.stream().map(this::mapWalletToDto).toList();
    }
}
