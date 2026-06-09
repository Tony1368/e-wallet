package com.hust.thailq.accounting.repository;

import com.hust.thailq.accounting.domain.entity.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    List<JournalEntry> findByTransactionId(UUID transactionId);

    Page<JournalEntry> findByCreatedAtBetween(Instant start, Instant end, Pageable pageable);

    List<JournalEntry> findByCreatedAtBetweenAndErpTransferredFalse(Instant start, Instant end);
}
