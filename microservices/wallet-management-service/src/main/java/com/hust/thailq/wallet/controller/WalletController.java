package com.hust.thailq.wallet.controller;

import com.hust.thailq.wallet.dto.request.TransactionRequest;
import com.hust.thailq.wallet.dto.request.UpdateWalletStatusRequest;
import com.hust.thailq.wallet.dto.request.WalletRequest;
import com.hust.thailq.wallet.dto.response.CommandResponse;
import com.hust.thailq.wallet.dto.response.WalletResponse;
import com.hust.thailq.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.findById(id));
    }

    @GetMapping("/iban/{iban}")
    public ResponseEntity<WalletResponse> findByIban(@PathVariable String iban) {
        return ResponseEntity.ok(walletService.findByIban(iban));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<WalletResponse>> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.findByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<Page<WalletResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(walletService.findAll(pageable));
    }

    @PostMapping
    public ResponseEntity<CommandResponse> create(@Valid @RequestBody WalletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommandResponse> update(@PathVariable Long id, @Valid @RequestBody WalletRequest request) {
        return ResponseEntity.ok(walletService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        walletService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<Void> updateWalletStatus(@PathVariable Long id, @RequestBody UpdateWalletStatusRequest request) {
        walletService.updateStatus(id, request.getStatus());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<CommandResponse> transferFunds(@Valid @RequestBody TransactionRequest request) {
        System.out.println("Transfer request received: " + request);
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.transferFunds(request));
    }

    @PostMapping("/add")
    public ResponseEntity<CommandResponse> addFunds(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.addFunds(request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<CommandResponse> withdrawFunds(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.withdrawFunds(request));
    }
}
