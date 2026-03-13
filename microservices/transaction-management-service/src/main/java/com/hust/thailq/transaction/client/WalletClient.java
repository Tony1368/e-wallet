package com.hust.thailq.transaction.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class WalletClient {

    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${services.wallet.url:http://wallet-service:8082}")
    private String walletServiceUrl;

    public WalletDto getWallet(Long walletId) {
        try {
            return restTemplate.getForObject(
                walletServiceUrl + "/api/v1/wallets/" + walletId, 
                WalletDto.class
            );
        } catch (Exception e) {
            return null;
        }
    }

    @Data
    public static class WalletDto {
        private Long id;
        private String iban;
        private String name;
        private BigDecimal balance;
        private Long userId;
        private String status;
    }
}
