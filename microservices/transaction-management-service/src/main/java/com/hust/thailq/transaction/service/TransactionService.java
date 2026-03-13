package com.hust.thailq.transaction.service;

import com.hust.thailq.transaction.client.WalletClient;
import com.hust.thailq.transaction.domain.entity.Transaction;
import com.hust.thailq.transaction.domain.enums.Status;
import com.hust.thailq.transaction.dto.request.TransactionRequest;
import com.hust.thailq.transaction.dto.response.TransactionResponse;
import com.hust.thailq.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletClient walletClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @Transactional(readOnly = true)
    public TransactionResponse findById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        return toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponse findByReferenceNumber(UUID referenceNumber) {
        Transaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new RuntimeException("Transaction not found with reference: " + referenceNumber));
        return toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findAllByUserId(Long userId) {
        // Get all transactions - in real implementation, filter by user's wallets
        // For now, return all transactions as demo
        return transactionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> findAll(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setFromWalletId(request.getFromWalletId());
        transaction.setToWalletId(request.getToWalletId());
        transaction.setTypeId(request.getTypeId() != null ? request.getTypeId() : 1L);
        transaction.setStatus(Status.SUCCESS);
        
        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    private TransactionResponse toResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setDescription(transaction.getDescription());
        response.setCreatedAt(FORMATTER.format(transaction.getCreatedAt()));
        response.setReferenceNumber(transaction.getReferenceNumber());
        response.setStatus(transaction.getStatus());
        response.setFromWalletId(transaction.getFromWalletId());
        response.setToWalletId(transaction.getToWalletId());
        
        // Populate wallet info - simplified without user info for now
        TransactionResponse.WalletInfo fromWallet = new TransactionResponse.WalletInfo();
        fromWallet.setId(transaction.getFromWalletId());
        TransactionResponse.UserInfo fromUser = new TransactionResponse.UserInfo();
        fromUser.setFirstName("User");
        fromUser.setLastName(String.valueOf(transaction.getFromWalletId()));
        fromWallet.setUser(fromUser);
        response.setFromWallet(fromWallet);
        
        TransactionResponse.WalletInfo toWallet = new TransactionResponse.WalletInfo();
        toWallet.setId(transaction.getToWalletId());
        TransactionResponse.UserInfo toUser = new TransactionResponse.UserInfo();
        toUser.setFirstName("User");
        toUser.setLastName(String.valueOf(transaction.getToWalletId()));
        toWallet.setUser(toUser);
        response.setToWallet(toWallet);
        
        TransactionResponse.TypeInfo type = new TransactionResponse.TypeInfo();
        type.setId(transaction.getTypeId());
        type.setName("Transfer");
        response.setType(type);
        
        return response;
    }
}
