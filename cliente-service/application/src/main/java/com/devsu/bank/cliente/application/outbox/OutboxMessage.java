package com.devsu.bank.cliente.application.outbox;

import java.time.Instant;
import java.util.UUID;

/**
 * One row of the outbox table. {@code id} is the physical-message identity sent as the {@code
 * outbox-message-id} header: stable across retries, it is the consumer dedup key.
 */
public record OutboxMessage<T>(
    UUID id,
    String origin,
    String topic,
    String messageKey,
    T payload,
    OutboxStatus status,
    int attempts,
    String error,
    Instant occurredAt,
    Instant nextAttemptAt,
    Instant publishedAt,
    String traceparent) {

  public static final int ERROR_MAX = 512;

  public OutboxMessage<T> published(Instant at) {
    return with(OutboxStatus.PUBLISHED, attempts, null, null, at);
  }

  public OutboxMessage<T> attemptFailed(String reason, RetryPolicy policy, Instant now) {
    int attempt = attempts + 1;
    String error = truncate(reason);
    if (policy.isExhausted(attempt)) {
      return with(OutboxStatus.FAILED, attempt, error, null, null);
    }
    return with(OutboxStatus.PENDING, attempt, error, now.plus(policy.delayAfter(attempt)), null);
  }

  /** Back to the queue untouched: it was never handed to the broker. */
  public OutboxMessage<T> released() {
    return with(OutboxStatus.PENDING, attempts, error, nextAttemptAt, publishedAt);
  }

  public <R> OutboxMessage<R> withPayload(R newPayload) {
    return new OutboxMessage<>(
        id,
        origin,
        topic,
        messageKey,
        newPayload,
        status,
        attempts,
        error,
        occurredAt,
        nextAttemptAt,
        publishedAt,
        traceparent);
  }

  private OutboxMessage<T> with(
      OutboxStatus newStatus, int newAttempts, String newError, Instant next, Instant published) {
    return new OutboxMessage<>(
        id,
        origin,
        topic,
        messageKey,
        payload,
        newStatus,
        newAttempts,
        newError,
        occurredAt,
        next,
        published,
        traceparent);
  }

  private static String truncate(String reason) {
    String text = reason == null ? "unknown" : reason;
    return text.length() > ERROR_MAX ? text.substring(0, ERROR_MAX) : text;
  }
}
