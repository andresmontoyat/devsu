package com.devsu.bank.cliente.application.outbox;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class OutboxTestData {

  public static final Instant AHORA = Instant.parse("2022-02-10T15:00:00Z");
  public static final String ORIGIN = "cliente-service";
  public static final String TOPIC = "clientes.cliente.events";
  public static final RetryPolicy RETRY =
      new RetryPolicy(3, Duration.ofSeconds(30), Duration.ofMinutes(1));

  private OutboxTestData() {}

  public static OutboxMessage<String> pendiente(String key) {
    return new OutboxMessage<>(
        UUID.randomUUID(),
        ORIGIN,
        TOPIC,
        key,
        "{}",
        OutboxStatus.PENDING,
        0,
        null,
        AHORA,
        null,
        null,
        null);
  }

  public static OutboxMessage<String> conIntentos(String key, int intentos) {
    OutboxMessage<String> base = pendiente(key);
    return new OutboxMessage<>(
        base.id(),
        base.origin(),
        base.topic(),
        base.messageKey(),
        base.payload(),
        OutboxStatus.IN_FLIGHT,
        intentos,
        null,
        base.occurredAt(),
        null,
        null,
        null);
  }
}
