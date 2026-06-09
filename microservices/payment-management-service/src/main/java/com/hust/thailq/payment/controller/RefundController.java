package com.hust.thailq.payment.controller;

import com.hust.thailq.payment.client.WalletClient;
import com.hust.thailq.payment.client.TransactionClient;
import com.hust.thailq.payment.domain.entity.RefundRequest;
import com.hust.thailq.payment.dto.request.TransactionRequest;
import com.hust.thailq.payment.dto.response.CommandResponse;
import com.hust.thailq.payment.repository.RefundRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class RefundController {

    private final RefundRequestRepository refundRequestRepository;
    private final WalletClient walletClient;
    private final TransactionClient transactionClient;

    @PostMapping("/refund-request")
    public ResponseEntity<CommandResponse> createRefundRequest(@RequestBody Map<String, Object> body) {
        RefundRequest request = new RefundRequest();
        request.setTransactionId(String.valueOf(body.get("transactionId")));
        request.setWalletId(body.get("walletId") != null ? Long.parseLong(String.valueOf(body.get("walletId"))) : 0L);
        request.setAmount(body.get("amount") != null ? new java.math.BigDecimal(String.valueOf(body.get("amount"))) : java.math.BigDecimal.ZERO);
        request.setReason((String) body.get("reason"));
        request.setRequestedBy((String) body.get("requestedBy"));
        request.setStatus("PENDING");

        RefundRequest saved = refundRequestRepository.save(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommandResponse.builder().id(saved.getId()).message("Refund request created").build());
    }

    @GetMapping("/refund-requests/pending")
    public ResponseEntity<List<RefundRequest>> getPendingRequests() {
        return ResponseEntity.ok(refundRequestRepository.findByStatusOrderByCreatedAtDesc("PENDING"));
    }

    @PostMapping("/refund-requests/{id}/approve")
    public ResponseEntity<CommandResponse> approveRefund(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        RefundRequest request = refundRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Refund request not found: " + id));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Refund request is not in PENDING status");
        }

        // Credit back to wallet
        if (request.getWalletId() != null && request.getWalletId() > 0 && request.getAmount() != null) {
            walletClient.credit(request.getWalletId(), request.getAmount());

            // Record refund transaction
            TransactionRequest txRequest = new TransactionRequest();
            txRequest.setAmount(request.getAmount());
            txRequest.setDescription("Refund approved for TX#" + request.getTransactionId());
            txRequest.setFromWalletId(request.getWalletId());
            txRequest.setToWalletId(request.getWalletId());
            txRequest.setTypeId(7L); // Refund type
            transactionClient.createTransaction(txRequest);
        }

        request.setStatus("APPROVED");
        request.setApprovedBy(body != null ? body.get("approvedBy") : "MANAGER");
        request.setUpdatedAt(Instant.now());
        refundRequestRepository.save(request);

        return ResponseEntity.ok(CommandResponse.builder().id(id).message("Refund approved").build());
    }

    @PostMapping("/refund-requests/{id}/reject")
    public ResponseEntity<CommandResponse> rejectRefund(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        RefundRequest request = refundRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Refund request not found: " + id));

        request.setStatus("REJECTED");
        request.setApprovedBy(body != null ? body.get("rejectedBy") : "MANAGER");
        request.setUpdatedAt(Instant.now());
        refundRequestRepository.save(request);

        return ResponseEntity.ok(CommandResponse.builder().id(id).message("Refund rejected").build());
    }
}
