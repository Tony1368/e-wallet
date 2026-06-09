package com.hust.thailq.accounting.controller;

import com.hust.thailq.accounting.domain.entity.JournalEntry;
import com.hust.thailq.accounting.repository.JournalEntryRepository;
import com.hust.thailq.accounting.service.AccountingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounting")
@RequiredArgsConstructor
public class AccountingController {

    private final AccountingService accountingService;
    private final JournalEntryRepository journalEntryRepository;

    @GetMapping("/journal-entries/{transactionId}")
    public ResponseEntity<List<JournalEntry>> getByTransactionId(@PathVariable UUID transactionId) {
        return ResponseEntity.ok(accountingService.findByTransactionId(transactionId));
    }

    @GetMapping("/journal-entries")
    public ResponseEntity<Page<JournalEntry>> getAllEntries(Pageable pageable,
            @RequestParam(required = false) String date) {
        if (date != null && !date.isEmpty()) {
            LocalDate ld = LocalDate.parse(date);
            Instant start = ld.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant end = ld.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            return ResponseEntity.ok(journalEntryRepository.findByCreatedAtBetween(start, end, pageable));
        }
        return ResponseEntity.ok(journalEntryRepository.findAll(pageable));
    }

    @PostMapping("/erp-transfer")
    public ResponseEntity<Map<String, Object>> erpTransfer(@RequestBody Map<String, String> body) {
        String date = body.get("date");
        LocalDate ld = LocalDate.parse(date);
        Instant start = ld.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = ld.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<JournalEntry> entries = journalEntryRepository.findByCreatedAtBetweenAndErpTransferredFalse(start, end);

        Instant now = Instant.now();
        for (JournalEntry entry : entries) {
            entry.setErpTransferred(true);
            entry.setErpTransferredAt(now);
        }
        journalEntryRepository.saveAll(entries);

        return ResponseEntity.ok(Map.of(
                "message", "ERP transfer completed",
                "date", date,
                "transferredCount", entries.size(),
                "transferredAt", now.toString()
        ));
    }
}
