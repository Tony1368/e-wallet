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
        try {
            kafkaTemplate.send(TOPIC, event.getTransactionId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish TransactionCompletedEvent: transactionId={}, error={}",
                                    event.getTransactionId(), ex.getMessage());
                        } else {
                            log.info("Published TransactionCompletedEvent: transactionId={}, offset={}",
                                    event.getTransactionId(), result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            log.error("Kafka unavailable, event lost: tx={}, error={}", event.getTransactionId(), e.getMessage());
        }
    }
}
