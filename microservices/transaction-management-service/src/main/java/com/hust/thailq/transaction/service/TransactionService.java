package com.hust.thailq.transaction.service;

import com.hust.thailq.transaction.client.WalletClient;
import com.hust.thailq.transaction.domain.entity.Transaction;
import com.hust.thailq.transaction.domain.enums.Status;
import com.hust.thailq.transaction.dto.request.TransactionRequest;
import com.hust.thailq.transaction.dto.response.TransactionResponse;
import com.hust.thailq.transaction.event.TransactionCompletedEvent;
import com.hust.thailq.transaction.event.TransactionEventProducer;
import com.hust.thailq.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletClient walletClient;
    private final TransactionEventProducer transactionEventProducer;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @Transactional(readOnly = true)
    public TransactionResponse findById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        return toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponse findByReferenceNumber(UUID referenceNumber) {
        Transaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new RuntimeException("Transaction not found with reference: " + referenceNumber));
        return toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> findAllByUserId(Long userId, Pageable pageable) {
        return transactionRepository.findByWalletId(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> findByWalletIds(java.util.List<Long> walletIds, Pageable pageable) {
        return transactionRepository.findByWalletIds(walletIds, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> findAll(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getWalletStats(Long walletId) {
        Instant startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant oneMinuteAgo = Instant.now().minusSeconds(60);

        int dailyCount = transactionRepository.countByFromWalletIdAndCreatedAtAfter(walletId, startOfDay);
        BigDecimal dailyTotal = transactionRepository.sumAmountByFromWalletIdAndCreatedAtAfter(walletId, startOfDay);
        int lastMinuteCount = transactionRepository.countByFromWalletIdAndCreatedAtAfter(walletId, oneMinuteAgo);
        BigDecimal avgAmount = transactionRepository.avgAmountByFromWalletId(walletId);

        return Map.of(
                "dailyTransactionCount", dailyCount,
                "dailyTotalAmount", dailyTotal,
                "transactionsInLastMinute", lastMinuteCount,
                "averageTransactionAmount", avgAmount
        );
    }

    public long count() {
        return transactionRepository.count();
    }

    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setFromWalletId(request.getFromWalletId());
        transaction.setToWalletId(request.getToWalletId());
        transaction.setTypeId(request.getTypeId() != null ? request.getTypeId() : 1L);
        transaction.setStatus(Status.SUCCESS);
        
        Transaction saved = transactionRepository.save(transaction);

        // Publish event to Kafka for async processing (accounting, notifications, etc.)
        TransactionCompletedEvent event = new TransactionCompletedEvent(
                saved.getReferenceNumber(),
                saved.getAmount(),
                saved.getFromWalletId(),
                saved.getToWalletId(),
                saved.getDescription(),
                saved.getTypeId()
        );
        transactionEventProducer.publishTransactionCompleted(event);

        return toResponse(saved);
    }

    private TransactionResponse toResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setDescription(transaction.getDescription());
        response.setCreatedAt(FORMATTER.format(transaction.getCreatedAt()));
        response.setReferenceNumber(transaction.getReferenceNumber());
        response.setStatus(transaction.getStatus());
        response.setFromWalletId(transaction.getFromWalletId());
        response.setToWalletId(transaction.getToWalletId());

        // Fetch real wallet info
        response.setFromWallet(buildWalletInfo(transaction.getFromWalletId()));
        response.setToWallet(buildWalletInfo(transaction.getToWalletId()));

        TransactionResponse.TypeInfo type = new TransactionResponse.TypeInfo();
        type.setId(transaction.getTypeId());
        switch (transaction.getTypeId().intValue()) {
            case 1 -> type.setName("Chuyển điểm");
            case 4 -> type.setName("Nạp điểm");
            case 5 -> type.setName("Rút điểm");
            case 6 -> type.setName("Đổi thưởng");
            case 7 -> type.setName("Hoàn điểm");
            case 8 -> type.setName("Cấp điểm batch");
            default -> type.setName("Khác");
        }
        response.setType(type);

        return response;
    }

    private TransactionResponse.WalletInfo buildWalletInfo(Long walletId) {
        TransactionResponse.WalletInfo info = new TransactionResponse.WalletInfo();
        info.setId(walletId);

        WalletClient.WalletDto wallet = walletClient.getWallet(walletId);
        if (wallet != null) {
            info.setName(wallet.getName());
            info.setIban(wallet.getIban());
            TransactionResponse.UserInfo userInfo = new TransactionResponse.UserInfo();
            userInfo.setFirstName(wallet.getName());
            userInfo.setLastName("");
            info.setUser(userInfo);
        } else {
            TransactionResponse.UserInfo userInfo = new TransactionResponse.UserInfo();
            userInfo.setFirstName("Ví #" + walletId);
            userInfo.setLastName("");
            info.setUser(userInfo);
        }
        return info;
    }
}
