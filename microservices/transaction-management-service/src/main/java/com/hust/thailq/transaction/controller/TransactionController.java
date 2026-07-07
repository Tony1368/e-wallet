package com.hust.thailq.transaction.controller;

import com.hust.thailq.transaction.dto.request.TransactionRequest;
import com.hust.thailq.transaction.dto.response.TransactionResponse;
import com.hust.thailq.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.findById(id));
    }

    @GetMapping("/references/{referenceNumber}")
    public ResponseEntity<TransactionResponse> findByReferenceNumber(@PathVariable UUID referenceNumber) {
        return ResponseEntity.ok(transactionService.findByReferenceNumber(referenceNumber));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<TransactionResponse>> findAllByUserId(@PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(transactionService.findAllByUserId(userId, pageable));
    }

    @GetMapping("/wallets")
    public ResponseEntity<Page<TransactionResponse>> findByWalletIds(@RequestParam List<Long> ids, Pageable pageable) {
        return ResponseEntity.ok(transactionService.findByWalletIds(ids, pageable));
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(transactionService.findAll(pageable));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/stats/{walletId}")
    public ResponseEntity<java.util.Map<String, Object>> getWalletStats(@PathVariable Long walletId) {
        return ResponseEntity.ok(transactionService.getWalletStats(walletId));
    }

    @GetMapping("/count")
    public ResponseEntity<java.util.Map<String, Object>> count() {
        long total = transactionService.count();
        return ResponseEntity.ok(java.util.Map.of("count", total));
    }
}
