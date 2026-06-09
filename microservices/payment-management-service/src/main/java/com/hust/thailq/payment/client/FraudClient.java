package com.hust.thailq.payment.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FraudClient {

    private final RestTemplate restTemplate;

    @Value("${services.fraud.url:http://localhost:8086}")
    private String fraudServiceUrl;

    /**
     * Check transaction validity against fraud rules.
     * Returns true if transaction is FRAUDULENT (should be blocked).
     */
    public boolean isFraudulent(Long walletId, BigDecimal amount) {
        try {
            Map<String, Object> request = Map.of(
                    "walletId", walletId,
                    "amount", amount
            );
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    fraudServiceUrl + "/api/v1/fraud/check", request, Map.class);
            if (response != null && Boolean.TRUE.equals(response.get("fraudulent"))) {
                log.warn("Fraud detected for walletId={}, amount={}: {}", walletId, amount, response.get("reason"));
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Fraud check failed (allowing transaction): {}", e.getMessage());
            return false; // Fail-open: allow transaction if fraud service is down
        }
    }
}
