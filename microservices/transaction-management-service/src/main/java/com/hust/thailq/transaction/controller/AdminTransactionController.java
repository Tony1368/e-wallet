package com.hust.thailq.transaction.controller;

import com.hust.thailq.transaction.dto.response.TransactionResponse;
import com.hust.thailq.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/transactions")
@RequiredArgsConstructor
public class AdminTransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(transactionService.findAll(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TransactionResponse>> searchByUsername(@RequestParam String username, Pageable pageable) {
        // For now, return all transactions - in production filter by username via wallet ownership
        return ResponseEntity.ok(transactionService.findAll(pageable));
    }
}
