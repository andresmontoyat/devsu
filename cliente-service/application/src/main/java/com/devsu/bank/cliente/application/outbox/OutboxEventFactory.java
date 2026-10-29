package com.devsu.bank.cliente.application.outbox;

import com.devsu.bank.cliente.application.port.outbound.TraceIdSource;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Turns an {@link OutboxEvent} into a pending row: stamps origin, row id, timestamp and the current
 * trace context. Callers never set those (messaging.md §3.2).
 */
public final class OutboxEventFactory {

  private static final int TRACE_ID_INDEX = 1;

  private final String origin;
  private final Clock clock;
  private final TraceIdSource traceIdSource;

  public OutboxEventFactory(String origin, Clock clock, TraceIdSource traceIdSource) {
    this.origin = origin;
    this.clock = clock;
    this.traceIdSource = traceIdSource;
  }

  public <T> OutboxMessage<EventMessage<T>> create(OutboxEvent<T> event) {
    Instant now = clock.instant();
    Optional<String> traceparent = traceIdSource.currentTraceparent();
    EventMessage<T> envelope = envelope(event, now, traceparent);
    return new OutboxMessage<>(
        UUID.randomUUID(),
        origin,
        event.topic(),
        event.key(),
        envelope,
        OutboxStatus.PENDING,
        0,
        null,
        now,
        null,
        null,
        traceparent.orElse(null));
  }

  private <T> EventMessage<T> envelope(
      OutboxEvent<T> event, Instant now, Optional<String> traceparent) {
    String traceId = traceparent.map(tp -> tp.split("-")[TRACE_ID_INDEX]).orElse(null);
    EventMessage.Header header =
        new EventMessage.Header(
            traceId, EventMessage.ENVELOPE_VERSION, event.schemaVersion(), origin, now, null);
    return new EventMessage<>(
        header,
        new EventMessage.Body<>(event.key(), event.type(), EventMessage.EVENT, event.payload()));
  }
}
