package com.hust.thailq.wallet.service;

import com.hust.thailq.wallet.domain.entity.Wallet;
import com.hust.thailq.wallet.domain.enums.WalletStatus;
import com.hust.thailq.wallet.dto.request.WalletRequest;
import com.hust.thailq.wallet.dto.response.CommandResponse;
import com.hust.thailq.wallet.dto.response.WalletResponse;
import com.hust.thailq.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @Transactional(readOnly = true)
    public WalletResponse findById(Long id) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + id));
        return toResponse(wallet);
    }

    @Transactional(readOnly = true)
    public WalletResponse findByIban(String iban) {
        Wallet wallet = walletRepository.findByIban(iban)
                .orElseThrow(() -> new RuntimeException("Wallet not found with iban: " + iban));
        return toResponse(wallet);
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> findByUserId(Long userId) {
        return walletRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<WalletResponse> findAll(Pageable pageable) {
        return walletRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public CommandResponse create(WalletRequest request) {
        if (walletRepository.existsByIban(request.getIban())) {
            throw new RuntimeException("Wallet already exists with iban: " + request.getIban());
        }

        Wallet wallet = new Wallet();
        wallet.setName(request.getName());
        wallet.setIban(request.getIban());
        wallet.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        wallet.setUserId(request.getUserId());
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setCreatedAt(Instant.now());

        Wallet saved = walletRepository.save(wallet);
        return CommandResponse.builder().id(saved.getId()).message("Wallet created successfully").build();
    }

    @Transactional
    public CommandResponse update(Long id, WalletRequest request) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + id));

        wallet.setName(request.getName());
        wallet.setBalance(request.getBalance());

        walletRepository.save(wallet);
        return CommandResponse.builder().id(wallet.getId()).message("Wallet updated successfully").build();
    }

    @Transactional
    public void deleteById(Long id) {
        if (!walletRepository.existsById(id)) {
            throw new RuntimeException("Wallet not found with id: " + id);
        }
        walletRepository.deleteById(id);
    }

    @Transactional
    public void updateStatus(Long id, WalletStatus status) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + id));
        wallet.setStatus(status);
        walletRepository.save(wallet);
    }

    @Transactional
    public void updateBalance(Long id, BigDecimal newBalance) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + id));
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
    }

    private WalletResponse toResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setIban(wallet.getIban());
        response.setName(wallet.getName());
        response.setBalance(wallet.getBalance());
        response.setUserId(wallet.getUserId());
        response.setCreatedAt(FORMATTER.format(wallet.getCreatedAt()));
        response.setBankInfo(wallet.getBankInfo());
        response.setStatus(wallet.getStatus());
        return response;
    }
}
