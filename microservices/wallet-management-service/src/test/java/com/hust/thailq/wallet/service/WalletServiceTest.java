package com.hust.thailq.wallet.service;

import com.hust.thailq.wallet.domain.entity.Wallet;
import com.hust.thailq.wallet.domain.enums.WalletStatus;
import com.hust.thailq.wallet.dto.request.WalletRequest;
import com.hust.thailq.wallet.dto.response.CommandResponse;
import com.hust.thailq.wallet.dto.response.WalletResponse;
import com.hust.thailq.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private WalletService walletService;

    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testWallet = new Wallet();
        testWallet.setId(1L);
        testWallet.setName("Test Wallet");
        testWallet.setIban("VN0001");
        testWallet.setBalance(new BigDecimal("1000000"));
        testWallet.setUserId(1L);
        testWallet.setStatus(WalletStatus.ACTIVE);
        testWallet.setCreatedAt(Instant.now());
    }

    @Test
    @DisplayName("TC01 - Tim vi theo ID thanh cong")
    void findById_success() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("wallet:balance:1")).thenReturn(null);

        WalletResponse result = walletService.findById(1L);

        assertNotNull(result);
        assertEquals("Test Wallet", result.getName());
        assertEquals("VN0001", result.getIban());
    }

    @Test
    @DisplayName("TC02 - Tim vi theo ID khong ton tai")
    void findById_notFound() {
        when(walletRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> walletService.findById(999L));
    }

    @Test
    @DisplayName("TC03 - Tao vi moi thanh cong")
    void create_success() {
        WalletRequest request = new WalletRequest();
        request.setName("New Wallet");
        request.setIban("VN0002");
        request.setBalance(new BigDecimal("500000"));
        request.setUserId(1L);

        when(walletRepository.existsByIban("VN0002")).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        CommandResponse result = walletService.create(request);

        assertNotNull(result);
        assertEquals("Wallet created successfully", result.getMessage());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    @DisplayName("TC04 - Tao vi trung IBAN that bai")
    void create_duplicateIban() {
        WalletRequest request = new WalletRequest();
        request.setIban("VN0001");

        when(walletRepository.existsByIban("VN0001")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> walletService.create(request));
        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC05 - Debit thanh cong qua Redis")
    void debit_success() {
        when(redisTemplate.hasKey("wallet:balance:1")).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("wallet:balance:1", -50000L)).thenReturn(50000L);

        assertDoesNotThrow(() -> walletService.debit(1L, new BigDecimal("500")));
    }

    @Test
    @DisplayName("TC06 - Debit that bai khi so du khong du")
    void debit_insufficientBalance() {
        when(redisTemplate.hasKey("wallet:balance:1")).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("wallet:balance:1", -200000000L)).thenReturn(-100000000L);
        when(valueOperations.increment("wallet:balance:1", 200000000L)).thenReturn(100000000L);

        assertThrows(RuntimeException.class,
                () -> walletService.debit(1L, new BigDecimal("2000000")));
    }

    @Test
    @DisplayName("TC07 - Credit thanh cong qua Redis")
    void credit_success() {
        when(redisTemplate.hasKey("wallet:balance:1")).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("wallet:balance:1", 100000L)).thenReturn(200000L);

        assertDoesNotThrow(() -> walletService.credit(1L, new BigDecimal("1000")));
    }
}
