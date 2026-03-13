package com.hust.thailq.dto.response;

import lombok.Data;

/**
 * Data Transfer Object for UserSession response.
 */
@Data
public class UserSessionResponse {

    private Long id;
    private String sessionId;
    private String loginTime;
    private String logoutTime;
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
    private Boolean isActive;
    private UserResponse user;
} 