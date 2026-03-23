package com.hust.thailq.payment.client;

import com.hust.thailq.payment.dto.response.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WalletClient {

    private final RestTemplate restTemplate;

    @Value("${services.wallet.url:http://localhost:8082}")
    private String walletServiceUrl;

    public WalletResponse getWalletByIban(String iban) {
        return restTemplate.getForObject(
                walletServiceUrl + "/api/v1/wallets/iban/{iban}", WalletResponse.class, iban);
    }

    public WalletResponse getWalletById(Long id) {
        return restTemplate.getForObject(
                walletServiceUrl + "/api/v1/wallets/{id}", WalletResponse.class, id);
    }

    public void updateBalance(Long walletId, BigDecimal newBalance) {
        restTemplate.put(
                walletServiceUrl + "/api/v1/wallets/{id}/balance",
                Map.of("balance", newBalance), walletId);
    }
}
