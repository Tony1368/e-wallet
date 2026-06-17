package com.hust.thailq.accounting.controller;

import com.hust.thailq.accounting.domain.entity.JournalEntry;
import com.hust.thailq.accounting.repository.JournalEntryRepository;
import com.hust.thailq.accounting.service.AccountingService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String type) {
        if (fromDate != null && !fromDate.isEmpty() && toDate != null && !toDate.isEmpty()) {
            Instant start = LocalDate.parse(fromDate).atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant end = LocalDate.parse(toDate).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            if (type != null && !type.isEmpty()) {
                return ResponseEntity.ok(journalEntryRepository.findByCreatedAtBetweenAndTransactionType(start, end, type, pageable));
            }
            return ResponseEntity.ok(journalEntryRepository.findByCreatedAtBetween(start, end, pageable));
        }
        if (date != null && !date.isEmpty()) {
            LocalDate ld = LocalDate.parse(date);
            Instant start = ld.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant end = ld.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            if (type != null && !type.isEmpty()) {
                return ResponseEntity.ok(journalEntryRepository.findByCreatedAtBetweenAndTransactionType(start, end, type, pageable));
            }
            return ResponseEntity.ok(journalEntryRepository.findByCreatedAtBetween(start, end, pageable));
        }
        if (type != null && !type.isEmpty()) {
            return ResponseEntity.ok(journalEntryRepository.findByTransactionType(type, pageable));
        }
        return ResponseEntity.ok(journalEntryRepository.findAll(pageable));
    }

    @GetMapping("/journal-entries/export")
    public void exportCsv(HttpServletResponse response,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) throws Exception {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=journal_entries.csv");
        response.setCharacterEncoding("UTF-8");

        List<JournalEntry> entries;
        if (fromDate != null && !fromDate.isEmpty() && toDate != null && !toDate.isEmpty()) {
            Instant start = LocalDate.parse(fromDate).atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant end = LocalDate.parse(toDate).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            entries = journalEntryRepository.findByCreatedAtBetween(start, end);
        } else if (date != null && !date.isEmpty()) {
            LocalDate ld = LocalDate.parse(date);
            Instant start = ld.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant end = ld.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            entries = journalEntryRepository.findByCreatedAtBetween(start, end);
        } else {
            entries = journalEntryRepository.findAll();
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        PrintWriter writer = response.getWriter();
        writer.println("ID,Mã giao dịch,Loại,Từ ví,Đến ví,Số tiền,Mô tả,Thời gian,Kết chuyển ERP");
        for (JournalEntry e : entries) {
            writer.printf("%d,%s,%s,%d,%d,%s,\"%s\",%s,%s%n",
                    e.getId(), e.getTransactionId(), e.getEntryType(),
                    e.getFromWalletId(), e.getToWalletId(), e.getAmount(),
                    e.getDescription() != null ? e.getDescription().replace("\"", "'") : "",
                    fmt.format(e.getCreatedAt()),
                    e.getErpTransferred() ? "Đã kết chuyển" : "Chưa");
        }
        writer.flush();
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
