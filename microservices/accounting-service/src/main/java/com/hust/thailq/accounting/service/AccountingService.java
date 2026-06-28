package com.hust.thailq.accounting.service;

import com.hust.thailq.accounting.domain.entity.JournalEntry;
import com.hust.thailq.accounting.domain.entity.Ledger;
import com.hust.thailq.accounting.event.TransactionCompletedEvent;
import com.hust.thailq.accounting.repository.JournalEntryRepository;
import com.hust.thailq.accounting.repository.LedgerRepository;
import com.hust.thailq.common.audit.AuditLog;
import com.hust.thailq.common.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountingService {

    private final LedgerRepository ledgerRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final AuditLogService auditLogService;
    private static final String DEFAULT_LEDGER = "GENERAL";

    @KafkaListener(topics = "transaction-events", groupId = "accounting-group")
    @Transactional
    public void handleTransactionCompleted(@Payload TransactionCompletedEvent event,
                                           @Headers Map<String, Object> headers) {
        // Extract requestId from Kafka header for distributed tracing
        Object requestIdHeader = headers.get("X-Request-Id");
        String requestId = null;
        if (requestIdHeader instanceof byte[] bytes) {
            requestId = new String(bytes, StandardCharsets.UTF_8);
            MDC.put("requestId", requestId);
        }

        long startTime = System.currentTimeMillis();
        String errorMsg = null;
        try {
        if (event == null || event.getTransactionId() == null) {
            log.warn("Received null or invalid TransactionCompletedEvent, skipping");
            return;
        }
        log.info("Received TransactionCompletedEvent: transactionId={}, amount={}", event.getTransactionId(), event.getAmount());

        Ledger ledger = ledgerRepository.findByName(DEFAULT_LEDGER)
                .orElseGet(() -> {
                    Ledger newLedger = new Ledger();
                    newLedger.setName(DEFAULT_LEDGER);
                    newLedger.setDescription("General Ledger");
                    return ledgerRepository.save(newLedger);
                });

        String txType = resolveTransactionType(event.getTypeId());

        // Debit entry (from wallet)
        JournalEntry debit = new JournalEntry();
        debit.setTransactionId(event.getTransactionId());
        debit.setLedger(ledger);
        debit.setFromWalletId(event.getFromWalletId());
        debit.setToWalletId(event.getToWalletId());
        debit.setAmount(event.getAmount());
        debit.setEntryType("DEBIT");
        debit.setDescription(event.getDescription());
        debit.setTransactionType(txType);

        // Credit entry (to wallet)
        JournalEntry credit = new JournalEntry();
        credit.setTransactionId(event.getTransactionId());
        credit.setLedger(ledger);
        credit.setFromWalletId(event.getFromWalletId());
        credit.setToWalletId(event.getToWalletId());
        credit.setAmount(event.getAmount());
        credit.setEntryType("CREDIT");
        credit.setDescription(event.getDescription());
        credit.setTransactionType(txType);

        journalEntryRepository.saveAll(List.of(debit, credit));
        log.info("Journal entries created for transactionId={}", event.getTransactionId());
        } catch (Exception e) {
            errorMsg = e.getMessage();
            throw e;
        } finally {
            // Audit log for Kafka event processing
            auditLogService.log(AuditLog.builder()
                    .requestId(requestId != null ? requestId : UUID.randomUUID().toString())
                    .serviceName("accounting-service")
                    .httpMethod("KAFKA")
                    .path("topic:transaction-events")
                    .statusCode(errorMsg == null ? 200 : 500)
                    .durationMs(System.currentTimeMillis() - startTime)
                    .level(errorMsg == null ? "INFO" : "ERROR")
                    .errorClass(errorMsg)
                    .requestBody(event != null ? "txId=" + event.getTransactionId() + ",amount=" + event.getAmount() : null)
                    .responseBody(errorMsg == null ? "DEBIT+CREDIT entries created" : errorMsg)
                    .timestamp(Instant.now())
                    .build());
            MDC.remove("requestId");
        }
    }

    @Transactional(readOnly = true)
    public List<JournalEntry> findByTransactionId(UUID transactionId) {
        return journalEntryRepository.findByTransactionId(transactionId);
    }
    private String resolveTransactionType(Long typeId) {
        if (typeId == null) return "KHAC";
        return switch (typeId.intValue()) {
            case 1 -> "CHUYEN_DIEM";
            case 4 -> "NAP_DIEM";
            case 5 -> "RUT_DIEM";
            case 6 -> "DOI_THUONG";
            case 7 -> "HOAN_DIEM";
            case 8 -> "CAP_DIEM_BATCH";
            default -> "KHAC";
        };
    }
}
