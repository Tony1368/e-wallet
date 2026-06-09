package com.hust.thailq.fraud.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudCheckRequest {

    private Long walletId;
    private BigDecimal amount;

    // Tan suat
    private Integer dailyTransactionCount;
    private BigDecimal dailyTotalAmount;
    private Integer transactionsInLastMinute;

    // Vi tri dia ly
    private String currentLatitude;
    private String currentLongitude;
    private String lastLatitude;
    private String lastLongitude;
    private Integer minutesSinceLastTransaction;

    // So tien trung binh lich su
    private BigDecimal averageTransactionAmount;
}
