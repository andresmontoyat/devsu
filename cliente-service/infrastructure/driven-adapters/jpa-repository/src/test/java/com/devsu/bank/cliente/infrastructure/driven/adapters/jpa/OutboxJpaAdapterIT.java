package com.devsu.bank.cliente.infrastructure.driven.adapters.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cliente.application.outbox.ClaimRequest;
import com.devsu.bank.cliente.application.outbox.EventMessage;
import com.devsu.bank.cliente.application.outbox.OutboxEvent;
import com.devsu.bank.cliente.application.outbox.OutboxEventFactory;
import com.devsu.bank.cliente.application.outbox.OutboxMessage;
import com.devsu.bank.cliente.application.outbox.OutboxMessageWritten;
import com.devsu.bank.cliente.application.outbox.OutboxStatus;
import com.devsu.bank.cliente.infrastructure.driven.adapters.jpa.adapter.OutboxJpaAdapter;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@RecordApplicationEvents
class OutboxJpaAdapterIT extends AbstractJpaIT {

  private static final String ORIGIN = "cliente-service";
  private static final ClaimRequest CLAIM =
      new ClaimRequest(ORIGIN, 10, Duration.ofMinutes(5), "test-instance");
  private static final OutboxEventFactory FACTORY =
      new OutboxEventFactory(ORIGIN, Clock.systemUTC(), Optional::empty);

  private record Payload(String nombre) {}

  @Autowired private OutboxJpaAdapter adapter;
  @Autowired private ApplicationEvents events;

  @Test
  void should_refuse_to_write_outside_a_transaction() {
    OutboxMessage<EventMessage<Payload>> mensaje = mensaje("cliente-1");

    assertThatThrownBy(() -> adapter.save(mensaje)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void should_store_the_envelope_as_json_and_announce_the_write() {
    OutboxMessage<EventMessage<Payload>> mensaje = guardar("cliente-1");

    String payload =
        jdbc.sql("SELECT payload FROM outbox_message WHERE id = ?")
            .param(mensaje.id())
            .query(String.class)
            .single();
    assertThat(payload).contains("\"eventName\":\"ClienteCreado\"").contains("\"nombre\":\"Jose\"");
    assertThat(events.stream(OutboxMessageWritten.class))
        .containsExactly(new OutboxMessageWritten(mensaje.id()));
  }

  @Test
  void should_claim_pending_rows_as_in_flight_in_insert_order() {
    OutboxMessage<?> uno = guardar("cliente-1");
    OutboxMessage<?> dos = guardar("cliente-2");

    List<OutboxMessage<String>> reclamados = adapter.claim(CLAIM);

    assertThat(reclamados).extracting(OutboxMessage::id).containsExactly(uno.id(), dos.id());
    assertThat(reclamados).allMatch(m -> m.payload().contains("ClienteCreado"));
    assertThat(estado(uno)).isEqualTo("IN_FLIGHT");
    assertThat(adapter.claim(CLAIM)).isEmpty();
  }

  @Test
  void should_hold_back_a_key_until_its_earlier_row_is_published() {
    OutboxMessage<?> primero = guardar("cliente-1");
    OutboxMessage<?> segundo = guardar("cliente-1");

    List<OutboxMessage<String>> primeraRonda = adapter.claim(CLAIM);
    adapter.update(primeraRonda.getFirst().published(Instant.now()));
    List<OutboxMessage<String>> segundaRonda = adapter.claim(CLAIM);

    assertThat(primeraRonda).extracting(OutboxMessage::id).containsExactly(primero.id());
    assertThat(segundaRonda).extracting(OutboxMessage::id).containsExactly(segundo.id());
  }

  @Test
  void should_reclaim_rows_left_in_flight_by_a_crashed_dispatcher() {
    OutboxMessage<?> mensaje = guardar("cliente-1");
    adapter.claim(CLAIM);

    List<OutboxMessage<String>> reclamados =
        adapter.claim(new ClaimRequest(ORIGIN, 10, Duration.ZERO, "otra-instancia"));

    assertThat(reclamados).extracting(OutboxMessage::id).containsExactly(mensaje.id());
  }

  @Test
  void should_not_claim_a_row_whose_retry_is_not_due_yet() {
    guardar("cliente-1");
    OutboxMessage<String> reclamado = adapter.claim(CLAIM).getFirst();

    adapter.update(
        reclamado.attemptFailed(
            "broker caido",
            new com.devsu.bank.cliente.application.outbox.RetryPolicy(
                5, Duration.ofHours(1), Duration.ofHours(1)),
            Instant.now()));

    assertThat(adapter.claim(CLAIM)).isEmpty();
    assertThat(estado(reclamado)).isEqualTo(OutboxStatus.PENDING.name());
  }

  @Test
  void should_record_when_the_send_started() {
    guardar("cliente-1");
    OutboxMessage<String> reclamado = adapter.claim(CLAIM).getFirst();

    adapter.markSendStarted(reclamado.id());

    assertThat(
            jdbc.sql("SELECT send_started_at IS NOT NULL FROM outbox_message WHERE id = ?")
                .param(reclamado.id())
                .query(Boolean.class)
                .single())
        .isTrue();
  }

  @Test
  void should_ignore_an_update_for_a_row_that_is_no_longer_in_flight() {
    guardar("cliente-1");
    OutboxMessage<String> reclamado = adapter.claim(CLAIM).getFirst();
    adapter.update(reclamado.published(Instant.now()));

    adapter.update(reclamado.released());

    assertThat(estado(reclamado)).isEqualTo("PUBLISHED");
  }

  private OutboxMessage<EventMessage<Payload>> guardar(String clave) {
    OutboxMessage<EventMessage<Payload>> mensaje = mensaje(clave);
    enTransaccion(
        () -> {
          adapter.save(mensaje);
          return null;
        });
    return mensaje;
  }

  private static OutboxMessage<EventMessage<Payload>> mensaje(String clave) {
    return FACTORY.create(
        OutboxEvent.to("clientes.cliente.events")
            .key(clave)
            .type("ClienteCreado")
            .payload(new Payload("Jose")));
  }

  private String estado(OutboxMessage<?> mensaje) {
    return jdbc.sql("SELECT status FROM outbox_message WHERE id = ?")
        .param(mensaje.id())
        .query(String.class)
        .single();
  }
}
