package com.devsu.bank.cliente.application.outbox;

import static com.devsu.bank.cliente.application.outbox.OutboxTestData.AHORA;
import static com.devsu.bank.cliente.application.outbox.OutboxTestData.ORIGIN;
import static com.devsu.bank.cliente.application.outbox.OutboxTestData.TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class OutboxEventFactoryTest {

  private static final String TRACEPARENT =
      "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01";
  private static final Clock CLOCK = Clock.fixed(AHORA, ZoneOffset.UTC);

  private record Payload(String valor) {}

  private static final OutboxEvent<Payload> EVENTO =
      OutboxEvent.to(TOPIC).key("cliente-1").type("ClienteCreado").payload(new Payload("x"));

  @Test
  void should_build_a_pending_row_with_the_canonical_envelope() {
    OutboxEventFactory factory = new OutboxEventFactory(ORIGIN, CLOCK, Optional::empty);

    OutboxMessage<EventMessage<Payload>> mensaje = factory.create(EVENTO);

    assertThat(mensaje.id()).isNotNull();
    assertThat(mensaje.status()).isEqualTo(OutboxStatus.PENDING);
    assertThat(mensaje.attempts()).isZero();
    assertThat(mensaje.topic()).isEqualTo(TOPIC);
    assertThat(mensaje.messageKey()).isEqualTo("cliente-1");
    assertThat(mensaje.origin()).isEqualTo(ORIGIN);
    assertThat(mensaje.occurredAt()).isEqualTo(AHORA);
    assertThat(mensaje.payload().header())
        .isEqualTo(
            new EventMessage.Header(
                null,
                EventMessage.ENVELOPE_VERSION,
                EventMessage.DEFAULT_SCHEMA_VERSION,
                ORIGIN,
                AHORA,
                null));
    assertThat(mensaje.payload().body())
        .isEqualTo(
            new EventMessage.Body<>(
                "cliente-1", "ClienteCreado", EventMessage.EVENT, new Payload("x")));
  }

  @Test
  void should_capture_the_current_trace_context_when_there_is_one() {
    OutboxEventFactory factory =
        new OutboxEventFactory(ORIGIN, CLOCK, () -> Optional.of(TRACEPARENT));

    OutboxMessage<EventMessage<Payload>> mensaje = factory.create(EVENTO);

    assertThat(mensaje.traceparent()).isEqualTo(TRACEPARENT);
    assertThat(mensaje.payload().header().traceId()).isEqualTo("4bf92f3577b34da6a3ce929d0e0e4736");
  }

  @Test
  void should_carry_an_explicit_schema_version() {
    OutboxEventFactory factory = new OutboxEventFactory(ORIGIN, CLOCK, Optional::empty);

    OutboxMessage<EventMessage<Payload>> mensaje = factory.create(EVENTO.schemaVersion("2.0.0"));

    assertThat(mensaje.payload().header().schemaVersion()).isEqualTo("2.0.0");
  }

  @Test
  void should_reject_an_event_without_topic_key_or_type() {
    assertThatThrownBy(() -> OutboxEvent.to(" ")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> OutboxEvent.to(TOPIC).key(null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> OutboxEvent.to(TOPIC).key("k").type(""))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> OutboxEvent.to(TOPIC).key("k").type("T").payload(null))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
