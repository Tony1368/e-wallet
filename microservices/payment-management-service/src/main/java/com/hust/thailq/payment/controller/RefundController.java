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
        String transactionId = String.valueOf(body.get("transactionId"));
        Long storeWalletId = body.get("walletId") != null ? Long.parseLong(String.valueOf(body.get("walletId"))) : 0L;

        // 1. Kiểm tra giao dịch tồn tại
        Map<String, Object> tx = transactionClient.getTransaction(Long.parseLong(transactionId));
        if (tx == null) {
            throw new RuntimeException("Giao dich khong ton tai: " + transactionId);
        }

        // 2. Kiểm tra giao dịch thuộc ví cửa hàng (toĐWallet phải là ví cửa hàng)
        Object toWalletId = tx.get("toWalletId");
        if (toWalletId == null || Long.parseLong(String.valueOf(toWalletId)) != storeWalletId) {
            throw new RuntimeException("Giao dich nay khong thuoc vi cua hang cua ban");
        }

        // 3. Kiểm tra đã hoàn chưa
        boolean alreadyRefunded = refundRequestRepository.existsByTransactionIdAndStatusIn(
                transactionId, List.of("PENDING", "APPROVED"));
        if (alreadyRefunded) {
            throw new RuntimeException("Giao dich nay da duoc hoan hoac dang cho duyet. Khong the hoan lan nua.");
        }

        // 4. Xác định số tiền hoàn
        java.math.BigDecimal txAmount = new java.math.BigDecimal(String.valueOf(tx.get("amount")));
        java.math.BigDecimal refundAmount = body.get("amount") != null
                ? new java.math.BigDecimal(String.valueOf(body.get("amount")))
                : java.math.BigDecimal.ZERO;

        // Nếu amount = 0 thì hoàn toàn bộ
        if (refundAmount.compareTo(java.math.BigDecimal.ZERO) == 0) {
            refundAmount = txAmount;
        }

        // Kiểm tra không vượt quá số tiền giao dịch
        if (refundAmount.compareTo(txAmount) > 0) {
            throw new RuntimeException("So tien hoan (" + refundAmount + ") vuot qua so tien giao dich (" + txAmount + ")");
        }

        RefundRequest request = new RefundRequest();
        request.setTransactionId(transactionId);
        request.setWalletId(storeWalletId);
        request.setAmount(refundAmount);
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
