package com.hust.thailq.ewallet.controller;

import com.hust.thailq.ewallet.entity.Wallet;
import com.hust.thailq.ewallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletRepository walletRepository;

    @GetMapping("/{id}/balance")
    public ResponseEntity<?> getBalance(@PathVariable Long id) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        return ResponseEntity.ok(Map.of(
                "id", wallet.getId(),
                "iban", wallet.getIban(),
                "name", wallet.getName(),
                "balance", wallet.getBalance()
        ));
    }
}
