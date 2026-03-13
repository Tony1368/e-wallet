package com.hust.thailq.dto.response;

import com.hust.thailq.domain.enums.Status;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Data Transfer Object for detailed Transaction response including tracking information.
 */
@Data
public class TransactionDetailResponse {

    private Long id;
    private BigDecimal amount;
    private String description;
    private String createdAt;
    private UUID referenceNumber;
    private Status status;
    private WalletResponse fromWallet;
    private WalletResponse toWallet;
    private TypeResponse type;
    
    // Tracking information
    private String ipAddress;
    private String userAgent;
    private String deviceType;
    private String browser;
    private String operatingSystem;
    private String country;
    private String city;
    private String region;
    private String latitude;
    private String longitude;
    private String timezone;
    private Boolean isSuccessful;
    private String errorMessage;
} 