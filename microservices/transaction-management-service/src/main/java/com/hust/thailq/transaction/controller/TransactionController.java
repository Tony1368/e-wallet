package com.hust.thailq.transaction.controller;

import com.hust.thailq.transaction.dto.request.TransactionRequest;
import com.hust.thailq.transaction.dto.response.TransactionResponse;
import com.hust.thailq.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Page<TransactionResponse>> findAllByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(new PageImpl<>(transactionService.findAllByUserId(userId)));
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
}
