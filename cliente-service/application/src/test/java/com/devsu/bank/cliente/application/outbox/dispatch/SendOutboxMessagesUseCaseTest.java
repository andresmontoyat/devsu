package com.devsu.bank.cliente.application.outbox.dispatch;

import static com.devsu.bank.cliente.application.outbox.OutboxTestData.AHORA;
import static com.devsu.bank.cliente.application.outbox.OutboxTestData.ORIGIN;
import static com.devsu.bank.cliente.application.outbox.OutboxTestData.RETRY;
import static com.devsu.bank.cliente.application.outbox.OutboxTestData.pendiente;
import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.bank.cliente.application.outbox.ClaimRequest;
import com.devsu.bank.cliente.application.outbox.OutboxDispatchSettings;
import com.devsu.bank.cliente.application.outbox.OutboxMessage;
import com.devsu.bank.cliente.application.outbox.OutboxStatus;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SendOutboxMessagesUseCaseTest {

  private static final int BATCH = 3;
  private static final ClaimRequest CLAIM =
      new ClaimRequest(ORIGIN, BATCH, Duration.ofMinutes(5), "cliente-service@host");

  private FakeDispatchStore store;
  private FakePublisher publisher;
  private SendOutboxMessagesUseCase useCase;

  @BeforeEach
  void setUp() {
    store = new FakeDispatchStore();
    publisher = new FakePublisher(store);
    OutboxDispatchSettings settings =
        new OutboxDispatchSettings(CLAIM, RETRY, Clock.fixed(AHORA, ZoneOffset.UTC));
    useCase = new SendOutboxMessagesUseCase(store, publisher, settings);
  }

  @Test
  void should_publish_every_claimed_row_and_mark_it_published() {
    OutboxMessage<String> uno = pendiente("a");
    OutboxMessage<String> dos = pendiente("b");
    store.agregar(uno);
    store.agregar(dos);

    useCase.execute();

    assertThat(publisher.publicados()).containsExactly(uno.id(), dos.id());
    assertThat(store.actualizado(uno.id()).status()).isEqualTo(OutboxStatus.PUBLISHED);
    assertThat(store.ultimaSolicitud()).isEqualTo(CLAIM);
  }

  @Test
  void should_record_the_send_start_before_calling_the_broker() {
    store.agregar(pendiente("a"));

    useCase.execute();

    assertThat(publisher.siempreMarcadoAntesDePublicar()).isTrue();
  }

  @Test
  void should_schedule_a_retry_when_the_broker_fails() {
    OutboxMessage<String> mensaje = pendiente("a");
    store.agregar(mensaje);
    publisher.fallarCon(mensaje.id());

    useCase.execute();

    OutboxMessage<String> actualizado = store.actualizado(mensaje.id());
    assertThat(actualizado.status()).isEqualTo(OutboxStatus.PENDING);
    assertThat(actualizado.attempts()).isEqualTo(1);
    assertThat(actualizado.error()).isEqualTo("broker caido");
  }

  @Test
  void should_hold_back_later_rows_of_a_key_whose_earlier_row_failed() {
    OutboxMessage<String> primero = pendiente("a");
    OutboxMessage<String> segundo = pendiente("a");
    OutboxMessage<String> otraClave = pendiente("b");
    store.agregar(primero);
    store.agregar(segundo);
    store.agregar(otraClave);
    publisher.fallarCon(primero.id());

    useCase.execute();

    assertThat(publisher.publicados()).containsExactly(otraClave.id());
    assertThat(store.actualizado(segundo.id()).status()).isEqualTo(OutboxStatus.PENDING);
    assertThat(store.actualizado(segundo.id()).attempts()).isZero();
  }

  @Test
  void should_ask_for_another_run_when_the_batch_came_back_full() {
    store.agregar(pendiente("a"));
    store.agregar(pendiente("b"));
    store.agregar(pendiente("c"));

    assertThat(useCase.execute()).isTrue();
  }

  @Test
  void should_not_ask_for_another_run_when_the_batch_was_partial() {
    store.agregar(pendiente("a"));

    assertThat(useCase.execute()).isFalse();
  }
}
