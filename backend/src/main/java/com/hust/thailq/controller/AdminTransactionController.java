package com.hust.thailq.controller;

import com.hust.thailq.domain.entity.Transaction;
import com.hust.thailq.dto.response.TransactionResponse;
import com.hust.thailq.dto.response.TransactionDetailResponse;
import com.hust.thailq.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/transactions")
@RequiredArgsConstructor
public class AdminTransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACCOUNTANT')")
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(Pageable pageable) {
        return ResponseEntity.ok(transactionService.findAll(pageable));
    }

    /**
     * Fetches detailed transaction information including tracking data by the given id.
     *
     * @param id
     * @return TransactionDetailResponse wrapped by ResponseEntity<T>
     */
    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACCOUNTANT')")
    public ResponseEntity<TransactionDetailResponse> getTransactionDetail(@PathVariable long id) {
        final TransactionDetailResponse response = transactionService.findDetailById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACCOUNTANT')")
    public ResponseEntity<List<TransactionResponse>> searchByUsername(@RequestParam String username) {
        final List<TransactionResponse> response = transactionService.findAllByUsername(username);
        return ResponseEntity.ok(response);
    }
} 