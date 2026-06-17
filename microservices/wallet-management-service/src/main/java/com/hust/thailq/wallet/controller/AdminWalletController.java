package com.hust.thailq.wallet.controller;

import com.hust.thailq.wallet.dto.response.WalletResponse;
import com.hust.thailq.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/wallets")
@RequiredArgsConstructor
public class AdminWalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<Page<WalletResponse>> findAll(Pageable pageable,
            @RequestParam(required = false) Long branchId) {
        if (branchId != null) {
            return ResponseEntity.ok(walletService.findByBranchId(branchId, pageable));
        }
        return ResponseEntity.ok(walletService.findAll(pageable));
    }
}
