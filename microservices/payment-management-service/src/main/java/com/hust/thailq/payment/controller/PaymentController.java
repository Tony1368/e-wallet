package com.hust.thailq.payment.controller;

import com.hust.thailq.payment.dto.request.TransactionRequest;
import com.hust.thailq.payment.dto.response.CommandResponse;
import com.hust.thailq.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/transfer")
    public ResponseEntity<CommandResponse> transferFunds(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.transferFunds(request));
    }

    @PostMapping("/add")
    public ResponseEntity<CommandResponse> addFunds(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.addFunds(request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<CommandResponse> withdrawFunds(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.withdrawFunds(request));
    }
}
