package com.hust.thailq.wallet.service;

import com.hust.thailq.wallet.client.TransactionClient;
import com.hust.thailq.wallet.domain.entity.Wallet;
import com.hust.thailq.wallet.domain.enums.WalletStatus;
import com.hust.thailq.wallet.dto.request.RedeemRequest;
import com.hust.thailq.wallet.dto.request.TransactionRequest;
import com.hust.thailq.wallet.dto.request.WalletRequest;
import com.hust.thailq.wallet.dto.response.CommandResponse;
import com.hust.thailq.wallet.dto.response.WalletResponse;
import com.hust.thailq.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionClient transactionClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @Transactional(readOnly = true)
    public WalletResponse findById(Long id) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + id));
        return toResponse(wallet);
    }

    @Transactional(readOnly = true)
    public WalletResponse findByIban(String iban) {
        Wallet wallet = walletRepository.findByIban(iban)
                .orElseThrow(() -> new RuntimeException("Wallet not found with iban: " + iban));
        return toResponse(wallet);
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> findByUserId(Long userId) {
        return walletRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<WalletResponse> findAll(Pageable pageable) {
        return walletRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public CommandResponse create(WalletRequest request) {
        if (walletRepository.existsByIban(request.getIban())) {
            throw new RuntimeException("Wallet already exists with iban: " + request.getIban());
        }

        Wallet wallet = new Wallet();
        wallet.setName(request.getName());
        wallet.setIban(request.getIban());
        wallet.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        wallet.setUserId(request.getUserId());
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setCreatedAt(Instant.now());

        Wallet saved = walletRepository.save(wallet);
        return CommandResponse.builder().id(saved.getId()).message("Wallet created successfully").build();
    }

    @Transactional
    public CommandResponse update(Long id, WalletRequest request) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + id));

        wallet.setName(request.getName());
        wallet.setBalance(request.getBalance());

        walletRepository.save(wallet);
        return CommandResponse.builder().id(wallet.getId()).message("Wallet updated successfully").build();
    }

    @Transactional
    public void deleteById(Long id) {
        if (!walletRepository.existsById(id)) {
            throw new RuntimeException("Wallet not found with id: " + id);
        }
        walletRepository.deleteById(id);
    }

    @Transactional
    public void updateStatus(Long id, WalletStatus status) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + id));
        wallet.setStatus(status);
        walletRepository.save(wallet);
    }

    @Transactional
    public CommandResponse transferFunds(TransactionRequest request) {
        Wallet fromWallet = walletRepository.findByIban(request.getFromWalletIban())
                .orElseThrow(() -> new RuntimeException("From wallet not found"));
        Wallet toWallet = walletRepository.findByIban(request.getToWalletIban())
                .orElseThrow(() -> new RuntimeException("To wallet not found"));

        if (fromWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        fromWallet.setBalance(fromWallet.getBalance().subtract(request.getAmount()));
        toWallet.setBalance(toWallet.getBalance().add(request.getAmount()));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        // Create transaction record
        TransactionRequest txRequest = new TransactionRequest();
        txRequest.setAmount(request.getAmount());
        txRequest.setDescription(request.getDescription());
        txRequest.setFromWalletIban(request.getFromWalletIban());
        txRequest.setToWalletIban(request.getToWalletIban());
        txRequest.setFromWalletId(fromWallet.getId());
        txRequest.setToWalletId(toWallet.getId());
        txRequest.setTypeId(request.getTypeId() != null ? request.getTypeId() : 1L);
        transactionClient.createTransaction(txRequest);

        return CommandResponse.builder()
                .id(fromWallet.getId())
                .message("Transfer successful")
                .build();
    }

    @Transactional
    public CommandResponse addFunds(TransactionRequest request) {
        Wallet wallet = walletRepository.findByIban(request.getToWalletIban())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        walletRepository.save(wallet);

        return CommandResponse.builder()
                .id(wallet.getId())
                .message("Funds added successfully")
                .build();
    }

    @Transactional
    public CommandResponse withdrawFunds(TransactionRequest request) {
        Wallet wallet = walletRepository.findByIban(request.getFromWalletIban())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        walletRepository.save(wallet);

        return CommandResponse.builder()
                .id(wallet.getId())
                .message("Withdrawal successful")
                .build();
    }

    @Transactional
    public CommandResponse redeemReward(RedeemRequest request) {
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // Mock reward points calculation - in real implementation, get from reward service
        // For now, assume each reward costs points equal to rewardId * 100 * quantity
        java.math.BigDecimal pointsNeeded = java.math.BigDecimal.valueOf(request.getRewardId() * 100L * request.getQuantity());

        if (wallet.getBalance().compareTo(pointsNeeded) < 0) {
            throw new RuntimeException("Insufficient points");
        }

        wallet.setBalance(wallet.getBalance().subtract(pointsNeeded));
        walletRepository.save(wallet);

        return CommandResponse.builder()
                .id(wallet.getId())
                .message("Reward redeemed successfully")
                .build();
    }

    private WalletResponse toResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setIban(wallet.getIban());
        response.setName(wallet.getName());
        response.setBalance(wallet.getBalance());
        response.setUserId(wallet.getUserId());
        response.setCreatedAt(FORMATTER.format(wallet.getCreatedAt()));
        response.setBankInfo(wallet.getBankInfo());
        response.setStatus(wallet.getStatus());
        return response;
    }
}
