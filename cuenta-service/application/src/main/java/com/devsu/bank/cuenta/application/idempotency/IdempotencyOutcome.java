package com.devsu.bank.cuenta.application.idempotency;

import com.devsu.bank.cuenta.domain.model.MovimientoId;

/** What the store knows about an {@code Idempotency-Key} at reservation time. */
public sealed interface IdempotencyOutcome {

  /** First time seen: this request owns the key and must execute. */
  record Reserved() implements IdempotencyOutcome {}

  /** Already executed with the same body: answer with the original movement. */
  record Replay(MovimientoId movimientoId) implements IdempotencyOutcome {}

  /** Another request with this key is still running. */
  record InProgress() implements IdempotencyOutcome {}

  /** The key was used before with a different body. */
  record FingerprintMismatch() implements IdempotencyOutcome {}
}
