package com.hust.thailq.payment.service;

import com.hust.thailq.payment.client.FraudClient;
import com.hust.thailq.payment.client.TransactionClient;
import com.hust.thailq.payment.client.WalletClient;
import com.hust.thailq.payment.dto.request.TransactionRequest;
import com.hust.thailq.payment.dto.response.CommandResponse;
import com.hust.thailq.payment.dto.response.WalletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private WalletClient walletClient;

    @Mock
    private TransactionClient transactionClient;

    @Mock
    private FraudClient fraudClient;

    @InjectMocks
    private PaymentService paymentService;

    private WalletResponse fromWallet;
    private WalletResponse toWallet;

    @BeforeEach
    void setUp() {
        fromWallet = new WalletResponse();
        fromWallet.setId(1L);
        fromWallet.setIban("VN0001");
        fromWallet.setBalance(new BigDecimal("5000000"));

        toWallet = new WalletResponse();
        toWallet.setId(2L);
        toWallet.setIban("VN0002");
        toWallet.setBalance(new BigDecimal("3000000"));
    }

    @Test
    @DisplayName("TC15 - Chuyen diem thanh cong (full flow)")
    void transferFunds_success() {
        TransactionRequest request = new TransactionRequest();
        request.setFromWalletIban("VN0001");
        request.setToWalletIban("VN0002");
        request.setAmount(new BigDecimal("100000"));
        request.setDescription("Transfer test");

        when(walletClient.getWalletByIban("VN0001")).thenReturn(fromWallet);
        when(walletClient.getWalletByIban("VN0002")).thenReturn(toWallet);
        when(fraudClient.isFraudulent(1L, new BigDecimal("100000"))).thenReturn(false);

        CommandResponse result = paymentService.transferFunds(request);

        assertNotNull(result);
        assertEquals("Transfer successful", result.getMessage());
        verify(walletClient).debit(1L, new BigDecimal("100000"));
        verify(walletClient).credit(2L, new BigDecimal("100000"));
        verify(transactionClient).createTransaction(any());
    }

    @Test
    @DisplayName("TC16 - Chuyen diem bi chan boi fraud detection")
    void transferFunds_blockedByFraud() {
        TransactionRequest request = new TransactionRequest();
        request.setFromWalletIban("VN0001");
        request.setToWalletIban("VN0002");
        request.setAmount(new BigDecimal("60000000"));

        when(walletClient.getWalletByIban("VN0001")).thenReturn(fromWallet);
        when(walletClient.getWalletByIban("VN0002")).thenReturn(toWallet);
        when(fraudClient.isFraudulent(1L, new BigDecimal("60000000"))).thenReturn(true);

        assertThrows(RuntimeException.class, () -> paymentService.transferFunds(request));
        verify(walletClient, never()).debit(any(), any());
        verify(walletClient, never()).credit(any(), any());
    }

    @Test
    @DisplayName("TC17 - Nap diem thanh cong")
    void addFunds_success() {
        TransactionRequest request = new TransactionRequest();
        request.setToWalletIban("VN0001");
        request.setAmount(new BigDecimal("500000"));
        request.setDescription("Add funds");

        when(walletClient.getWalletByIban("VN0001")).thenReturn(fromWallet);

        CommandResponse result = paymentService.addFunds(request);

        assertEquals("Funds added successfully", result.getMessage());
        verify(walletClient).credit(1L, new BigDecimal("500000"));
        verify(transactionClient).createTransaction(any());
    }

    @Test
    @DisplayName("TC18 - Rut diem thanh cong")
    void withdrawFunds_success() {
        TransactionRequest request = new TransactionRequest();
        request.setFromWalletIban("VN0001");
        request.setAmount(new BigDecimal("200000"));

        when(walletClient.getWalletByIban("VN0001")).thenReturn(fromWallet);
        when(fraudClient.isFraudulent(1L, new BigDecimal("200000"))).thenReturn(false);

        CommandResponse result = paymentService.withdrawFunds(request);

        assertEquals("Withdrawal successful", result.getMessage());
        verify(walletClient).debit(1L, new BigDecimal("200000"));
        verify(transactionClient).createTransaction(any());
    }

    @Test
    @DisplayName("TC19 - Rut diem bi chan boi fraud")
    void withdrawFunds_blockedByFraud() {
        TransactionRequest request = new TransactionRequest();
        request.setFromWalletIban("VN0001");
        request.setAmount(new BigDecimal("60000000"));

        when(walletClient.getWalletByIban("VN0001")).thenReturn(fromWallet);
        when(fraudClient.isFraudulent(1L, new BigDecimal("60000000"))).thenReturn(true);

        assertThrows(RuntimeException.class, () -> paymentService.withdrawFunds(request));
        verify(walletClient, never()).debit(any(), any());
    }

    @Test
    @DisplayName("TC20 - Doi thuong thanh cong")
    void redeemReward_success() {
        com.hust.thailq.payment.dto.request.RedeemRequest request =
                new com.hust.thailq.payment.dto.request.RedeemRequest();
        request.setWalletId(1L);
        request.setRewardId(1L);
        request.setQuantity(2);

        when(walletClient.getWalletById(1L)).thenReturn(fromWallet);

        CommandResponse result = paymentService.redeemReward(request);

        assertEquals("Reward redeemed successfully", result.getMessage());
        // 1 * 100 * 2 = 200 points
        verify(walletClient).debit(1L, new BigDecimal("200"));
        verify(transactionClient).createTransaction(any());
    }
}
