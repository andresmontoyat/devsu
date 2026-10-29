package com.devsu.bank.cliente.infrastructure.driven.adapters.kafka;

import com.devsu.bank.cliente.application.outbox.OutboxMessage;
import com.devsu.bank.cliente.application.port.outbound.MessagePublisherPort;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes one outbox row and waits for the broker acknowledgement: the dispatcher only marks a
 * row PUBLISHED after Kafka confirmed it (at-least-once). The aggregate key keeps per-client order.
 */
@Component
public class KafkaMessagePublisherAdapter implements MessagePublisherPort {

  public static final String OUTBOX_MESSAGE_ID = "outbox-message-id";
  public static final String TRACEPARENT = "traceparent";
  private static final String CONTENT_TYPE = "content-type";

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final PublisherSettings settings;

  public KafkaMessagePublisherAdapter(
      KafkaTemplate<String, String> kafkaTemplate, PublisherSettings settings) {
    this.kafkaTemplate = kafkaTemplate;
    this.settings = settings;
  }

  @Override
  public void publish(OutboxMessage<String> message) {
    try {
      kafkaTemplate
          .send(toRecord(message))
          .get(settings.sendTimeout().toMillis(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new MessagePublishException("Publicacion interrumpida", e);
    } catch (ExecutionException | TimeoutException | RuntimeException e) {
      throw new MessagePublishException("Kafka no confirmo el mensaje " + message.id(), e);
    }
  }

  private static ProducerRecord<String, String> toRecord(OutboxMessage<String> message) {
    ProducerRecord<String, String> record =
        new ProducerRecord<>(message.topic(), message.messageKey(), message.payload());
    Headers headers = record.headers();
    headers.add(OUTBOX_MESSAGE_ID, bytes(message.id().toString()));
    headers.add(CONTENT_TYPE, bytes("application/json"));
    if (message.traceparent() != null) {
      headers.add(TRACEPARENT, bytes(message.traceparent()));
    }
    return record;
  }

  private static byte[] bytes(String value) {
    return value.getBytes(StandardCharsets.UTF_8);
  }
}
