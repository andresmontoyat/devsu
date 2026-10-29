package com.devsu.bank.cuenta.infrastructure.entry.points.kafka;

import com.devsu.bank.cuenta.application.port.inbound.ProjectClienteEventPort;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Inbound adapter: deserializes and delegates, nothing else. The offset is acknowledged only after
 * the projection committed, so a crash in between means redelivery — which the use case dedups.
 */
@Component
public class ClienteEventListener {

  static final String OUTBOX_MESSAGE_ID = "outbox-message-id";

  private final ProjectClienteEventPort projectClienteEvent;
  private final ClienteEventMapper mapper;

  public ClienteEventListener(
      ProjectClienteEventPort projectClienteEvent, ClienteEventMapper mapper) {
    this.projectClienteEvent = projectClienteEvent;
    this.mapper = mapper;
  }

  @KafkaListener(topics = "${cuenta.consumer.topic}")
  public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
    projectClienteEvent.execute(mapper.toEvent(messageId(record), record.value()));
    ack.acknowledge();
  }

  private static UUID messageId(ConsumerRecord<String, String> record) {
    Header header = record.headers().lastHeader(OUTBOX_MESSAGE_ID);
    if (header == null) {
      return null;
    }
    try {
      return UUID.fromString(new String(header.value(), StandardCharsets.UTF_8));
    } catch (IllegalArgumentException e) {
      throw new InvalidClienteEventException("outbox-message-id no es un UUID", e);
    }
  }
}
