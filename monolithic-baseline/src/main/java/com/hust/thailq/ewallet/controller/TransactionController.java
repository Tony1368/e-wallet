package com.hust.thailq.ewallet.controller;

import com.hust.thailq.ewallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getByUserId(@PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(transactionRepository.findByFromWalletIdOrToWalletId(userId, userId, pageable));
    }
}
