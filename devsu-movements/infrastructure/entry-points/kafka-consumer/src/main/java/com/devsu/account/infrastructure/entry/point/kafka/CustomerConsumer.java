package com.devsu.account.infrastructure.entry.point.kafka;

import com.devsu.account.Customer;
import com.devsu.account.Movement;
import com.devsu.account.usecase.port.inbound.CustomerUseCasePort;
import com.devsu.account.usecase.port.inbound.MovementProcessUseCasePort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomerConsumer {

    private final CustomerUseCasePort customerUseCasePort;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "created.customers.events", groupId = "${spring.application.name}-group")
    public void onMessageCreateCustomer(@Payload JsonNode movementPayload, ConsumerRecord<String, Object> record) throws JsonProcessingException {
        log.info("Received customer message: key={}, headers={}, payload={}", record.key(), record.headers(), movementPayload);

        var customer = objectMapper.treeToValue(movementPayload, Customer.class);
        customerUseCasePort.createCustomer(customer);
    }
}
