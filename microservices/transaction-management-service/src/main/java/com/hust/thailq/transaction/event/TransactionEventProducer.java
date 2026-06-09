package com.hust.thailq.transaction.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventProducer {

    private final KafkaTemplate<String, TransactionCompletedEvent> kafkaTemplate;
    private static final String TOPIC = "transaction-events";

    public void publishTransactionCompleted(TransactionCompletedEvent event) {
        kafkaTemplate.send(TOPIC, event.getTransactionId().toString(), event);
        log.info("Published TransactionCompletedEvent: transactionId={}", event.getTransactionId());
    }
}
