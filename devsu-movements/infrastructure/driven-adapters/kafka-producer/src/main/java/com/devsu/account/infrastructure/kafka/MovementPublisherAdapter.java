package com.devsu.account.infrastructure.kafka;

import com.devsu.account.Movement;
import com.devsu.account.infrastructure.kafka.model.KafkaMessage;
import com.devsu.account.usecase.port.outbound.MovementPublisher;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MovementPublisherAdapter implements MovementPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void movementReceived(Movement movement) {
        var event = new KafkaMessage.Body<Movement>();
        event.setEventName("movement.received");
        event.setDomainEntity("movement");
        event.setEventTimestamp(LocalDateTime.now().toString());
        event.setEventData(movement);

        kafkaTemplate.send("movement.process.event", event);
        log.info("Movement received event sent to Kafka: {}", event);
    }
}
