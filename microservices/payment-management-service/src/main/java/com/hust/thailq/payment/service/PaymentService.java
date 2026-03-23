package com.hust.thailq.payment.service;

import com.hust.thailq.payment.client.TransactionClient;
import com.hust.thailq.payment.client.WalletClient;
import com.hust.thailq.payment.dto.request.RedeemRequest;
import com.hust.thailq.payment.dto.request.TransactionRequest;
import com.hust.thailq.payment.dto.response.CommandResponse;
import com.hust.thailq.payment.dto.response.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final WalletClient walletClient;
    private final TransactionClient transactionClient;

    public CommandResponse transferFunds(TransactionRequest request) {
        WalletResponse fromWallet = walletClient.getWalletByIban(request.getFromWalletIban());
        WalletResponse toWallet = walletClient.getWalletByIban(request.getToWalletIban());

        if (fromWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        walletClient.updateBalance(fromWallet.getId(),
                fromWallet.getBalance().subtract(request.getAmount()));
        walletClient.updateBalance(toWallet.getId(),
                toWallet.getBalance().add(request.getAmount()));

        // Create transaction record
        TransactionRequest txRequest = new TransactionRequest();
        txRequest.setAmount(request.getAmount());
        txRequest.setDescription(request.getDescription());
        txRequest.setFromWalletIban(request.getFromWalletIban());
        txRequest.setToWalletIban(request.getToWalletIban());
        txRequest.setFromWalletId(fromWallet.getId());
        txRequest.setToWalletId(toWallet.getId());
        txRequest.setTypeId(request.getTypeId() != null ? request.getTypeId() : 1L);
        transactionClient.createTransaction(txRequest);

        return CommandResponse.builder()
                .id(fromWallet.getId())
                .message("Transfer successful")
                .build();
    }

    public CommandResponse addFunds(TransactionRequest request) {
        WalletResponse wallet = walletClient.getWalletByIban(request.getToWalletIban());

        walletClient.updateBalance(wallet.getId(),
                wallet.getBalance().add(request.getAmount()));

        return CommandResponse.builder()
                .id(wallet.getId())
                .message("Funds added successfully")
                .build();
    }

    public CommandResponse withdrawFunds(TransactionRequest request) {
        WalletResponse wallet = walletClient.getWalletByIban(request.getFromWalletIban());

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        walletClient.updateBalance(wallet.getId(),
                wallet.getBalance().subtract(request.getAmount()));

        return CommandResponse.builder()
                .id(wallet.getId())
                .message("Withdrawal successful")
                .build();
    }

    public CommandResponse redeemReward(RedeemRequest request) {
        WalletResponse wallet = walletClient.getWalletById(request.getWalletId());

        BigDecimal pointsNeeded = BigDecimal.valueOf(request.getRewardId() * 100L * request.getQuantity());

        if (wallet.getBalance().compareTo(pointsNeeded) < 0) {
            throw new RuntimeException("Insufficient points");
        }

        walletClient.updateBalance(wallet.getId(),
                wallet.getBalance().subtract(pointsNeeded));

        return CommandResponse.builder()
                .id(wallet.getId())
                .message("Reward redeemed successfully")
                .build();
    }
}
