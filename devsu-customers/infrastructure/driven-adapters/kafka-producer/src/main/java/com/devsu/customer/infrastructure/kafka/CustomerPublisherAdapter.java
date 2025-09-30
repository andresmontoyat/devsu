package com.devsu.customer.infrastructure.kafka;

import com.devsu.customer.Customer;
import com.devsu.customer.infrastructure.kafka.model.KafkaMessage;
import com.devsu.customer.usecase.port.outbound.CustomerPublisher;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomerPublisherAdapter implements CustomerPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void customerCreated(Customer result) {
        var event = new KafkaMessage.Body<>();
        event.setEventName("customer.created");
        event.setDomainEntity("customer");
        event.setEventTimestamp(LocalDateTime.now().toString());
        event.setEventData(result);

        kafkaTemplate.send("created.customers.events", event);
        log.info("Customer created event sent to Kafka: {}", event);
    }

    @Override
    public void customerDeleted(UUID id) {
        var event = new KafkaMessage.Body<>();
        event.setEventName("customer.deleted");
        event.setDomainEntity("customer");
        event.setEventTimestamp(LocalDateTime.now().toString());
        event.setEventData(Map.of("id", id));

        kafkaTemplate.send("deleted.customers.events", event);
        log.info("Customer deleted event sent to Kafka: {}", event);
    }

    @Override
    public void customerStatusChanged(UUID id, boolean active) {
        var event = new KafkaMessage.Body<>();
        event.setEventName("customer.status.changed");
        event.setDomainEntity("customer");
        event.setEventTimestamp(LocalDateTime.now().toString());
        event.setEventData(Map.of("id", id, "active", active));

        kafkaTemplate.send("status.changes.customers.events", event);
        log.info("Customer status changed event sent to Kafka: {}", event);
    }
}
