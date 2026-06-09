package com.hust.thailq.fraud.service;

import com.hust.thailq.fraud.domain.entity.FraudRuleConfig;
import com.hust.thailq.fraud.dto.FraudCheckRequest;
import com.hust.thailq.fraud.dto.FraudCheckResponse;
import com.hust.thailq.fraud.repository.FraudRuleConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceTest {

    @Mock
    private FraudRuleConfigRepository ruleConfigRepository;

    @InjectMocks
    private FraudDetectionService fraudDetectionService;

    private FraudRuleConfig defaultRule;

    @BeforeEach
    void setUp() {
        defaultRule = new FraudRuleConfig();
        defaultRule.setId(1L);
        defaultRule.setRuleName("DEFAULT_RULE");
        defaultRule.setMaxTransactionAmount(new BigDecimal("50000000"));
        defaultRule.setMaxDailyTransactions(100);
        defaultRule.setMaxDailyAmount(new BigDecimal("200000000"));
        defaultRule.setMaxTransactionsPerMinute(10);
        defaultRule.setVelocityWindowSeconds(60);
        defaultRule.setGeoVelocityMinutes(30);
        defaultRule.setGeoVelocityEnabled(true);
        defaultRule.setAnomalyAmountMultiplier(new BigDecimal("3.00"));
        defaultRule.setAnomalyEnabled(true);
        defaultRule.setEnabled(true);
    }

    @Test
    @DisplayName("TC08 - Giao dich hop le khong bi chan")
    void checkTransaction_valid() {
        when(ruleConfigRepository.findByEnabledTrue()).thenReturn(List.of(defaultRule));

        FraudCheckRequest request = new FraudCheckRequest();
        request.setWalletId(1L);
        request.setAmount(new BigDecimal("100000"));

        FraudCheckResponse result = fraudDetectionService.checkTransaction(request);

        assertFalse(result.isFraudulent());
        assertNull(result.getReason());
    }

    @Test
    @DisplayName("TC09 - Chan khi so tien vuot gioi han")
    void checkTransaction_exceedsMaxAmount() {
        when(ruleConfigRepository.findByEnabledTrue()).thenReturn(List.of(defaultRule));

        FraudCheckRequest request = new FraudCheckRequest();
        request.setWalletId(1L);
        request.setAmount(new BigDecimal("60000000"));

        FraudCheckResponse result = fraudDetectionService.checkTransaction(request);

        assertTrue(result.isFraudulent());
        assertTrue(result.getReason().contains("vuot gioi han"));
    }

    @Test
    @DisplayName("TC10 - Chan khi so giao dich trong ngay vuot gioi han")
    void checkTransaction_exceedsDailyCount() {
        when(ruleConfigRepository.findByEnabledTrue()).thenReturn(List.of(defaultRule));

        FraudCheckRequest request = new FraudCheckRequest();
        request.setWalletId(1L);
        request.setAmount(new BigDecimal("1000"));
        request.setDailyTransactionCount(101);

        FraudCheckResponse result = fraudDetectionService.checkTransaction(request);

        assertTrue(result.isFraudulent());
        assertTrue(result.getReason().contains("giao dich trong ngay"));
    }

    @Test
    @DisplayName("TC11 - Chan khi tong so tien ngay vuot gioi han")
    void checkTransaction_exceedsDailyAmount() {
        when(ruleConfigRepository.findByEnabledTrue()).thenReturn(List.of(defaultRule));

        FraudCheckRequest request = new FraudCheckRequest();
        request.setWalletId(1L);
        request.setAmount(new BigDecimal("50000000"));
        request.setDailyTotalAmount(new BigDecimal("160000000"));

        FraudCheckResponse result = fraudDetectionService.checkTransaction(request);

        assertTrue(result.isFraudulent());
        assertTrue(result.getReason().contains("tien giao dich trong ngay"));
    }

    @Test
    @DisplayName("TC12 - Chan khi tan suat giao dich bat thuong")
    void checkTransaction_velocityExceeded() {
        when(ruleConfigRepository.findByEnabledTrue()).thenReturn(List.of(defaultRule));

        FraudCheckRequest request = new FraudCheckRequest();
        request.setWalletId(1L);
        request.setAmount(new BigDecimal("1000"));
        request.setTransactionsInLastMinute(15);

        FraudCheckResponse result = fraudDetectionService.checkTransaction(request);

        assertTrue(result.isFraudulent());
        assertTrue(result.getReason().contains("Tan suat"));
    }

    @Test
    @DisplayName("TC13 - Chan khi 2 vi tri >50km trong 10 phut")
    void checkTransaction_geoVelocityFraud() {
        when(ruleConfigRepository.findByEnabledTrue()).thenReturn(List.of(defaultRule));

        FraudCheckRequest request = new FraudCheckRequest();
        request.setWalletId(1L);
        request.setAmount(new BigDecimal("1000"));
        // Ha Noi
        request.setCurrentLatitude("21.0285");
        request.setCurrentLongitude("105.8542");
        // TP HCM (1700km)
        request.setLastLatitude("10.8231");
        request.setLastLongitude("106.6297");
        request.setMinutesSinceLastTransaction(10);

        FraudCheckResponse result = fraudDetectionService.checkTransaction(request);

        assertTrue(result.isFraudulent());
        assertTrue(result.getReason().contains("vi tri"));
    }

    @Test
    @DisplayName("TC14 - Chan khi so tien gap 5 lan trung binh")
    void checkTransaction_anomalyAmount() {
        when(ruleConfigRepository.findByEnabledTrue()).thenReturn(List.of(defaultRule));

        FraudCheckRequest request = new FraudCheckRequest();
        request.setWalletId(1L);
        request.setAmount(new BigDecimal("5000000"));
        request.setAverageTransactionAmount(new BigDecimal("500000")); // 10x > 3x threshold

        FraudCheckResponse result = fraudDetectionService.checkTransaction(request);

        assertTrue(result.isFraudulent());
        assertTrue(result.getReason().contains("bat thuong"));
    }
}
