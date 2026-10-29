package com.devsu.bank.cliente.application.outbox;

import java.time.Duration;

/** Exponential backoff: {@code baseDelay × 2^(attempt − 1)}, capped at {@code maxDelay}. */
public record RetryPolicy(int maxAttempts, Duration baseDelay, Duration maxDelay) {

  private static final int MAX_SHIFT = 30;

  public RetryPolicy {
    if (maxAttempts < 1) {
      throw new IllegalArgumentException("maxAttempts must be at least 1");
    }
  }

  public Duration delayAfter(int attempt) {
    int shift = Math.min(Math.max(attempt - 1, 0), MAX_SHIFT);
    Duration delay = baseDelay.multipliedBy(1L << shift);
    return delay.compareTo(maxDelay) > 0 ? maxDelay : delay;
  }

  public boolean isExhausted(int attempts) {
    return attempts >= maxAttempts;
  }
}
