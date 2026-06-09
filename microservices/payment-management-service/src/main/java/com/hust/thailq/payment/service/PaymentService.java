package com.hust.thailq.payment.service;

import com.hust.thailq.payment.client.FraudClient;
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
    private final FraudClient fraudClient;

    public CommandResponse transferFunds(TransactionRequest request) {
        WalletResponse fromWallet = walletClient.getWalletByIban(request.getFromWalletIban());
        WalletResponse toWallet = walletClient.getWalletByIban(request.getToWalletIban());

        // Step 1: Fraud check
        if (fraudClient.isFraudulent(fromWallet.getId(), request.getAmount())) {
            throw new RuntimeException("Transaction blocked by fraud detection rules");
        }

        // Step 2: Debit from source wallet (Redis atomic)
        walletClient.debit(fromWallet.getId(), request.getAmount());

        // Step 3: Credit to destination wallet (Redis atomic)
        walletClient.credit(toWallet.getId(), request.getAmount());

        // Step 4: Record transaction (triggers Kafka event → accounting-service)
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

        // Credit to wallet (Redis atomic)
        walletClient.credit(wallet.getId(), request.getAmount());

        // Record transaction → Kafka → accounting
        TransactionRequest txRequest = new TransactionRequest();
        txRequest.setAmount(request.getAmount());
        txRequest.setDescription(request.getDescription() != null ? request.getDescription() : "Add funds");
        txRequest.setFromWalletIban(request.getToWalletIban());
        txRequest.setToWalletIban(request.getToWalletIban());
        txRequest.setFromWalletId(wallet.getId());
        txRequest.setToWalletId(wallet.getId());
        txRequest.setTypeId(4L); // Add Funds type
        transactionClient.createTransaction(txRequest);

        return CommandResponse.builder()
                .id(wallet.getId())
                .message("Funds added successfully")
                .build();
    }

    public CommandResponse withdrawFunds(TransactionRequest request) {
        WalletResponse wallet = walletClient.getWalletByIban(request.getFromWalletIban());

        // Fraud check
        if (fraudClient.isFraudulent(wallet.getId(), request.getAmount())) {
            throw new RuntimeException("Withdrawal blocked by fraud detection rules");
        }

        // Debit from wallet (Redis atomic - will throw if insufficient)
        walletClient.debit(wallet.getId(), request.getAmount());

        // Record transaction → Kafka → accounting
        TransactionRequest txRequest = new TransactionRequest();
        txRequest.setAmount(request.getAmount());
        txRequest.setDescription(request.getDescription() != null ? request.getDescription() : "Withdraw funds");
        txRequest.setFromWalletIban(request.getFromWalletIban());
        txRequest.setToWalletIban(request.getFromWalletIban());
        txRequest.setFromWalletId(wallet.getId());
        txRequest.setToWalletId(wallet.getId());
        txRequest.setTypeId(5L); // Withdraw type
        transactionClient.createTransaction(txRequest);

        return CommandResponse.builder()
                .id(wallet.getId())
                .message("Withdrawal successful")
                .build();
    }

    public CommandResponse redeemReward(RedeemRequest request) {
        WalletResponse wallet = walletClient.getWalletById(request.getWalletId());

        BigDecimal pointsNeeded = BigDecimal.valueOf(request.getRewardId() * 100L * request.getQuantity());

        // Debit points (Redis atomic - will throw if insufficient)
        walletClient.debit(wallet.getId(), pointsNeeded);

        // Record transaction → Kafka → accounting
        TransactionRequest txRequest = new TransactionRequest();
        txRequest.setAmount(pointsNeeded);
        txRequest.setDescription("Redeem reward #" + request.getRewardId());
        txRequest.setFromWalletIban(null);
        txRequest.setToWalletIban(null);
        txRequest.setFromWalletId(wallet.getId());
        txRequest.setToWalletId(wallet.getId());
        txRequest.setTypeId(6L); // Redeem type
        transactionClient.createTransaction(txRequest);

        return CommandResponse.builder()
                .id(wallet.getId())
                .message("Reward redeemed successfully")
                .build();
    }
}
