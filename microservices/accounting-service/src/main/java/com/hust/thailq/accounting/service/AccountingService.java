package com.hust.thailq.accounting.service;

import com.hust.thailq.accounting.domain.entity.JournalEntry;
import com.hust.thailq.accounting.domain.entity.Ledger;
import com.hust.thailq.accounting.event.TransactionCompletedEvent;
import com.hust.thailq.accounting.repository.JournalEntryRepository;
import com.hust.thailq.accounting.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountingService {

    private final LedgerRepository ledgerRepository;
    private final JournalEntryRepository journalEntryRepository;
    private static final String DEFAULT_LEDGER = "GENERAL";

    @KafkaListener(topics = "transaction-events", groupId = "accounting-group")
    @Transactional
    public void handleTransactionCompleted(TransactionCompletedEvent event) {
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
