package com.hust.thailq.transaction.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventProducer {

    private final KafkaTemplate<String, TransactionCompletedEvent> kafkaTemplate;
    private static final String TOPIC = "transaction-events";

    public void publishTransactionCompleted(TransactionCompletedEvent event) {
        try {
            ProducerRecord<String, TransactionCompletedEvent> record =
                    new ProducerRecord<>(TOPIC, event.getTransactionId().toString(), event);

            // Propagate requestId via Kafka header for distributed tracing
            String requestId = MDC.get("requestId");
            if (requestId != null) {
                record.headers().add(new RecordHeader("X-Request-Id",
                        requestId.getBytes(StandardCharsets.UTF_8)));
            }

            kafkaTemplate.send(record)
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
