package com.hust.thailq.payment.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FraudClient {

    private final RestTemplate restTemplate;

    @Value("${services.fraud.url:http://localhost:8086}")
    private String fraudServiceUrl;

    @Value("${services.transaction.url:http://localhost:8083}")
    private String transactionServiceUrl;

    /**
     * Check transaction validity against fraud rules.
     * Gathers wallet stats from transaction-service, then calls fraud-service.
     * Returns true if transaction is FRAUDULENT (should be blocked).
     */
    public boolean isFraudulent(Long walletId, BigDecimal amount) {
        try {
            // Step 1: Get wallet transaction stats
            Map<String, Object> stats = getWalletStats(walletId);

            // Step 2: Build full fraud check request
            Map<String, Object> request = new HashMap<>();
            request.put("walletId", walletId);
            request.put("amount", amount);
            request.put("dailyTransactionCount", stats.get("dailyTransactionCount"));
            request.put("dailyTotalAmount", stats.get("dailyTotalAmount"));
            request.put("transactionsInLastMinute", stats.get("transactionsInLastMinute"));
            request.put("averageTransactionAmount", stats.get("averageTransactionAmount"));

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

    @SuppressWarnings("unchecked")
    private Map<String, Object> getWalletStats(Long walletId) {
        try {
            Map<String, Object> stats = restTemplate.getForObject(
                    transactionServiceUrl + "/api/v1/transactions/stats/" + walletId, Map.class);
            return stats != null ? stats : Map.of();
        } catch (Exception e) {
            log.debug("Failed to get wallet stats: {}", e.getMessage());
            return Map.of();
        }
    }
}
