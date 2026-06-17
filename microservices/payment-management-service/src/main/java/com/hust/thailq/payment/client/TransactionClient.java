package com.hust.thailq.payment.client;

import com.hust.thailq.payment.dto.request.TransactionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionClient {

    private final RestTemplate restTemplate;

    @Value("${services.transaction.url:http://localhost:8083}")
    private String transactionServiceUrl;

    public Map<String, Object> createTransaction(TransactionRequest request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    transactionServiceUrl + "/api/v1/transactions", request, Map.class);
            return response;
        } catch (Exception e) {
            log.error("Failed to create transaction record: {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public java.util.Map<String, Object> getTransaction(Long id) {
        try {
            return restTemplate.getForObject(
                    transactionServiceUrl + "/api/v1/transactions/" + id, java.util.Map.class);
        } catch (Exception e) {
            return null;
        }
    }
}
