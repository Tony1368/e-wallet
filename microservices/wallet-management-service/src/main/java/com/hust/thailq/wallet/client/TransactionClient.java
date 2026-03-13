package com.hust.thailq.wallet.client;

import com.hust.thailq.wallet.dto.request.TransactionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class TransactionClient {

    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${services.transaction.url:http://transaction-service:8083}")
    private String transactionServiceUrl;

    public void createTransaction(TransactionRequest request) {
        try {
            restTemplate.postForObject(
                transactionServiceUrl + "/api/v1/transactions", 
                request, 
                Object.class
            );
        } catch (Exception e) {
            System.err.println("Failed to create transaction record: " + e.getMessage());
        }
    }
}
