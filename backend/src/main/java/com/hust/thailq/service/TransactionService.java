package com.hust.thailq.service;

import com.hust.thailq.config.MessageSourceConfig;
import com.hust.thailq.domain.entity.Transaction;
import com.hust.thailq.dto.mapper.TransactionRequestMapper;
import com.hust.thailq.dto.mapper.TransactionResponseMapper;
import com.hust.thailq.dto.mapper.TransactionDetailResponseMapper;
import com.hust.thailq.dto.request.TransactionRequest;
import com.hust.thailq.dto.response.CommandResponse;
import com.hust.thailq.dto.response.TransactionResponse;
import com.hust.thailq.dto.response.TransactionDetailResponse;
import com.hust.thailq.exception.NoSuchElementFoundException;
import com.hust.thailq.repository.TransactionRepository;
import com.hust.thailq.service.ClientInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.hust.thailq.common.MessageKeys.*;

/**
 * Service used for Transaction related operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final MessageSourceConfig messageConfig;
    private final TransactionRepository transactionRepository;
    private final TransactionRequestMapper transactionRequestMapper;
    private final TransactionResponseMapper transactionResponseMapper;
    private final TransactionDetailResponseMapper transactionDetailResponseMapper;
    private final ClientInfoService clientInfoService;

    /**
     * Fetches a single transaction by the given id.
     *
     * @param id
     * @return TransactionResponse
     */
    @Transactional(readOnly = true)
    public TransactionResponse findById(long id) {
        return transactionRepository.findById(id)
                .map(transactionResponseMapper::toDto)
                .orElseThrow(() -> new NoSuchElementFoundException(messageConfig.getMessage(ERROR_TRANSACTION_NOT_FOUND)));
    }

    /**
     * Fetches a single transaction by the given referenceNumber.
     *
     * @param referenceNumber
     * @return TransactionResponse
     */
    @Transactional(readOnly = true)
    public TransactionResponse findByReferenceNumber(UUID referenceNumber) {
        return transactionRepository.findByReferenceNumber(referenceNumber)
                .map(transactionResponseMapper::toDto)
                .orElseThrow(() -> new NoSuchElementFoundException(messageConfig.getMessage(ERROR_TRANSACTION_NOT_FOUND)));
    }

    /**
     * Fetches detailed transaction information including tracking data by the given id.
     *
     * @param id
     * @return TransactionDetailResponse
     */
    @Transactional(readOnly = true)
    public TransactionDetailResponse findDetailById(long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementFoundException(messageConfig.getMessage(ERROR_TRANSACTION_NOT_FOUND)));
        
        return transactionDetailResponseMapper.toDto(transaction);
    }

    /**
     * Fetches all transaction by the given userId.
     *
     * @param userId
     * @return List of TransactionResponse
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> findAllByUserId(Long userId) {
        final List<Transaction> transactions = transactionRepository.findAllByUserId(userId);
        if (transactions.isEmpty())
            throw new NoSuchElementFoundException(messageConfig.getMessage(ERROR_NO_RECORDS));

        return transactions.stream().map(transactionResponseMapper::toDto)
                .toList();
    }

    /**
     * Fetches all transactions based on the given paging and sorting parameters.
     *
     * @param pageable
     * @return List of TransactionResponse
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> findAll(Pageable pageable) {
        final Page<Transaction> transactions = transactionRepository.findAllOrderByCreatedAtDesc(pageable);
        if (transactions.isEmpty())
            throw new NoSuchElementFoundException(messageConfig.getMessage(ERROR_NO_RECORDS));

        return transactions.map(transactionResponseMapper::toDto);
    }

    /**
     * Fetches all transactions by the given username.
     *
     * @param username
     * @return List of TransactionResponse
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> findAllByUsername(String username) {
        final List<Transaction> transactions = transactionRepository.findAllByUsername(username);
        if (transactions.isEmpty())
            throw new NoSuchElementFoundException(messageConfig.getMessage(ERROR_NO_RECORDS));
        return transactions.stream().map(transactionResponseMapper::toDto).toList();
    }

    /**
     * Creates a new transaction using the given request parameters.
     *
     * @param request
     * @return id of the created transaction
     */
    public CommandResponse create(TransactionRequest request) {
        final Transaction transaction = transactionRequestMapper.toEntity(request);
        
        // Populate tracking information if not provided in the request
        if (transaction.getIpAddress() == null) {
            transaction.setIpAddress(clientInfoService.getClientIpAddress());
        }
        if (transaction.getUserAgent() == null) {
            transaction.setUserAgent(clientInfoService.getUserAgent());
        }
        if (transaction.getBrowser() == null || transaction.getOperatingSystem() == null || transaction.getDeviceType() == null) {
            String userAgent = transaction.getUserAgent() != null ? transaction.getUserAgent() : clientInfoService.getUserAgent();
            var deviceInfo = clientInfoService.parseUserAgent(userAgent);
            if (transaction.getBrowser() == null) {
                transaction.setBrowser(deviceInfo.get("browser"));
            }
            if (transaction.getOperatingSystem() == null) {
                transaction.setOperatingSystem(deviceInfo.get("operatingSystem"));
            }
            if (transaction.getDeviceType() == null) {
                transaction.setDeviceType(deviceInfo.get("deviceType"));
            }
        }
        
        transactionRepository.save(transaction);
        log.info(messageConfig.getMessage(INFO_TRANSACTION_CREATED, transaction.getFromWallet().getIban(), transaction.getToWallet().getIban(), transaction.getAmount()));
        return CommandResponse.builder().id(transaction.getId()).build();
    }

    /**
     * Creates a new transaction using the given request parameters and returns the entity.
     *
     * @param request
     * @return Transaction entity
     */
    public Transaction createAndReturnEntity(TransactionRequest request) {
        final Transaction transaction = transactionRequestMapper.toEntity(request);
        
        // Populate tracking information if not provided in the request
        if (transaction.getIpAddress() == null) {
            transaction.setIpAddress(clientInfoService.getClientIpAddress());
        }
        if (transaction.getUserAgent() == null) {
            transaction.setUserAgent(clientInfoService.getUserAgent());
        }
        if (transaction.getBrowser() == null || transaction.getOperatingSystem() == null || transaction.getDeviceType() == null) {
            String userAgent = transaction.getUserAgent() != null ? transaction.getUserAgent() : clientInfoService.getUserAgent();
            var deviceInfo = clientInfoService.parseUserAgent(userAgent);
            if (transaction.getBrowser() == null) {
                transaction.setBrowser(deviceInfo.get("browser"));
            }
            if (transaction.getOperatingSystem() == null) {
                transaction.setOperatingSystem(deviceInfo.get("operatingSystem"));
            }
            if (transaction.getDeviceType() == null) {
                transaction.setDeviceType(deviceInfo.get("deviceType"));
            }
        }
        
        transactionRepository.save(transaction);
        log.info(messageConfig.getMessage(INFO_TRANSACTION_CREATED, transaction.getFromWallet().getIban(), transaction.getToWallet().getIban(), transaction.getAmount()));
        return transaction;
    }

    /**
     * Saves an updated transaction entity.
     *
     * @param transaction
     * @return Transaction
     */
    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public TransactionRequestMapper getTransactionRequestMapper() {
        return transactionRequestMapper;
    }
}
