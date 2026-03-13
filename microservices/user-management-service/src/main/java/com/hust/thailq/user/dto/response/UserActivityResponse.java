package com.hust.thailq.user.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Data Transfer Object for UserActivity response.
 */
@Data
public class UserActivityResponse {

    private Long id;
    private String activityId;
    private String activityTime;
    private String activityType;
    private String description;
    private BigDecimal amount;
    private String fromWalletIban;
    private String toWalletIban;
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
    private UserResponse user;
    private UserSessionResponse session;
} 