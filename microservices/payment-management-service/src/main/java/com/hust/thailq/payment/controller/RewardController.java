package com.hust.thailq.payment.controller;

import com.hust.thailq.payment.dto.request.RedeemRequest;
import com.hust.thailq.payment.dto.response.CommandResponse;
import com.hust.thailq.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final PaymentService paymentService;

    @PostMapping("/redeem")
    public ResponseEntity<CommandResponse> redeemReward(@Valid @RequestBody RedeemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.redeemReward(request));
    }
}
