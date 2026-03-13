package com.hust.thailq.controller;

import com.hust.thailq.dto.response.WalletResponse;
import com.hust.thailq.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/wallets")
@RequiredArgsConstructor
public class AdminWalletController {

    private final WalletService walletService;

    /**
     * Fetches all wallets based on the given paging and sorting parameters.
     * Only accessible by users with ROLE_ADMIN or ROLE_ACCOUNTANT.
     *
     * @param pageable
     * @return List of WalletResponse wrapped by ResponseEntity<T>
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACCOUNTANT')")
    @GetMapping
    public ResponseEntity<Page<WalletResponse>> findAll(Pageable pageable) {
        final Page<WalletResponse> response = walletService.findAll(pageable);
        return ResponseEntity.ok(response);
    }
} 