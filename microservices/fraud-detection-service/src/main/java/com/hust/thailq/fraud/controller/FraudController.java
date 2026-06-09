package com.hust.thailq.fraud.controller;

import com.hust.thailq.fraud.dto.FraudCheckRequest;
import com.hust.thailq.fraud.dto.FraudCheckResponse;
import com.hust.thailq.fraud.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
public class FraudController {

    private final FraudDetectionService fraudDetectionService;

    @PostMapping("/check")
    public ResponseEntity<FraudCheckResponse> checkTransaction(@RequestBody FraudCheckRequest request) {
        return ResponseEntity.ok(fraudDetectionService.checkTransaction(request));
    }
}
