package com.devsu.account.infrastructure.entry.point.kafka;

import com.devsu.account.Movement;
import com.devsu.account.usecase.port.inbound.MovementProcessUseCasePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MovementConsumer {

    private final MovementProcessUseCasePort movementProcessUseCasePort;

    @KafkaListener(topics = "movement.process.event", groupId = "${spring.application.name}-group")
    public void onMessage(@Payload Movement movement, ConsumerRecord<String, Object> record) {
        log.info("Received movement message: key={}, headers={}, payload={}", record.key(), record.headers(), movement);
        movementProcessUseCasePort.process(movement);
    }
}
