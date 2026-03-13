package com.hust.thailq.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {
    
    // API Versioning
    public static final String API_VERSION = "/api/v1";
    
    // CORS
    public static final String ALLOWED_ORIGIN = "http://localhost:3000";
    
    // Date Formats
    public static final String DATE_FORMAT = "dd.MM.yyyy";
    public static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";
    
    // IBAN Validation
    public static final int IBAN_MIN_SIZE = 15;
    public static final int IBAN_MAX_SIZE = 34;
    public static final long IBAN_MAX = 999999999;
    public static final long IBAN_MODULUS = 97;
    
    // Tracing
    public static final String TRACE = "trace";
    
    // Service Names
    public static final String USER_SERVICE = "user-management-service";
    public static final String WALLET_SERVICE = "wallet-management-service";
    public static final String TRANSACTION_SERVICE = "transaction-management-service";
    public static final String PAYMENT_SERVICE = "payment-management-service";
}
