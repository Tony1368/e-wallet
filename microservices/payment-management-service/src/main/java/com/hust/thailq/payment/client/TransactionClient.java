package com.hust.thailq.payment.client;

import com.hust.thailq.payment.dto.request.TransactionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionClient {

    private final RestTemplate restTemplate;

    @Value("${services.transaction.url:http://localhost:8083}")
    private String transactionServiceUrl;

    public void createTransaction(TransactionRequest request) {
        try {
            restTemplate.postForObject(
                    transactionServiceUrl + "/api/v1/transactions", request, Object.class);
        } catch (Exception e) {
            log.error("Failed to create transaction record: {}", e.getMessage());
        }
    }
}
