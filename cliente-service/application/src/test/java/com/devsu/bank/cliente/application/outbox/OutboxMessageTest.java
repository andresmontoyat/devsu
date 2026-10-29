package com.devsu.bank.cliente.application.outbox;

import static com.devsu.bank.cliente.application.outbox.OutboxTestData.AHORA;
import static com.devsu.bank.cliente.application.outbox.OutboxTestData.RETRY;
import static com.devsu.bank.cliente.application.outbox.OutboxTestData.conIntentos;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class OutboxMessageTest {

  private static final String CLAVE = "cliente-1";

  @Test
  void should_be_published_with_its_timestamp_when_sent() {
    OutboxMessage<String> publicado = conIntentos(CLAVE, 1).published(AHORA);

    assertThat(publicado.status()).isEqualTo(OutboxStatus.PUBLISHED);
    assertThat(publicado.publishedAt()).isEqualTo(AHORA);
    assertThat(publicado.error()).isNull();
  }

  @Test
  void should_go_back_to_pending_with_backoff_when_attempts_remain() {
    OutboxMessage<String> fallido = conIntentos(CLAVE, 0).attemptFailed("timeout", RETRY, AHORA);

    assertThat(fallido.status()).isEqualTo(OutboxStatus.PENDING);
    assertThat(fallido.attempts()).isEqualTo(1);
    assertThat(fallido.error()).isEqualTo("timeout");
    assertThat(fallido.nextAttemptAt()).isEqualTo(AHORA.plus(Duration.ofSeconds(30)));
  }

  @Test
  void should_fail_permanently_when_the_last_attempt_fails() {
    OutboxMessage<String> fallido = conIntentos(CLAVE, 2).attemptFailed("timeout", RETRY, AHORA);

    assertThat(fallido.status()).isEqualTo(OutboxStatus.FAILED);
    assertThat(fallido.attempts()).isEqualTo(3);
    assertThat(fallido.nextAttemptAt()).isNull();
  }

  @Test
  void should_truncate_long_errors_to_the_column_size() {
    String largo = "x".repeat(OutboxMessage.ERROR_MAX + 50);

    OutboxMessage<String> fallido = conIntentos(CLAVE, 0).attemptFailed(largo, RETRY, AHORA);

    assertThat(fallido.error()).hasSize(OutboxMessage.ERROR_MAX);
  }

  @Test
  void should_return_to_pending_without_charging_an_attempt_when_released() {
    OutboxMessage<String> liberado = conIntentos(CLAVE, 1).released();

    assertThat(liberado.status()).isEqualTo(OutboxStatus.PENDING);
    assertThat(liberado.attempts()).isEqualTo(1);
  }

  @Test
  void should_keep_every_other_field_when_the_payload_is_replaced() {
    OutboxMessage<String> original = conIntentos(CLAVE, 1);

    OutboxMessage<Integer> convertido = original.withPayload(42);

    assertThat(convertido.payload()).isEqualTo(42);
    assertThat(convertido.id()).isEqualTo(original.id());
    assertThat(convertido.attempts()).isEqualTo(original.attempts());
  }
}
