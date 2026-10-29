package com.devsu.bank.cliente.application.outbox;

import java.time.Duration;

/**
 * One claim run: up to {@code batchSize} due rows of {@code origin}. Rows stuck {@code IN_FLIGHT}
 * longer than {@code claimTtl} belong to a crashed dispatcher and are reclaimed.
 */
public record ClaimRequest(String origin, int batchSize, Duration claimTtl, String claimedBy) {

  public ClaimRequest {
    if (batchSize < 1) {
      throw new IllegalArgumentException("batchSize must be at least 1");
    }
  }
}
