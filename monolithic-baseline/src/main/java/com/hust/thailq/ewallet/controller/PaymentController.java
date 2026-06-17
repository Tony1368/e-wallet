package com.hust.thailq.ewallet.controller;

import com.hust.thailq.ewallet.entity.Transaction;
import com.hust.thailq.ewallet.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody Map<String, Object> body) {
        Long fromWalletId = Long.parseLong(String.valueOf(body.get("fromWalletId")));
        Long toWalletId = Long.parseLong(String.valueOf(body.get("toWalletId")));
        BigDecimal amount = new BigDecimal(String.valueOf(body.get("amount")));
        String description = (String) body.get("description");

        Transaction tx = paymentService.transfer(fromWalletId, toWalletId, amount, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", tx.getId(),
                "referenceNumber", tx.getReferenceNumber(),
                "status", tx.getStatus(),
                "message", "Transfer successful"
        ));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addFunds(@RequestBody Map<String, Object> body) {
        Long walletId = Long.parseLong(String.valueOf(body.get("walletId")));
        BigDecimal amount = new BigDecimal(String.valueOf(body.get("amount")));
        String description = (String) body.get("description");

        paymentService.addFunds(walletId, amount, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Funds added successfully"));
    }
}
