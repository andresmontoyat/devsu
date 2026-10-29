package com.devsu.bank.cuenta.application.port.outbound;

import com.devsu.bank.cuenta.application.idempotency.IdempotencyOutcome;
import com.devsu.bank.cuenta.domain.model.MovimientoId;

/**
 * Remembers {@code Idempotency-Key}s of movement requests. Implementations throw {@link
 * com.devsu.bank.cuenta.application.idempotency.IdempotencyStoreNoDisponibleException} when the
 * store cannot answer: a key the client sent must never be silently ignored.
 */
public interface IdempotencyStorePort {

  /** Claims the key for this request, or reports what an earlier request with it did. */
  IdempotencyOutcome reserve(String key, String fingerprint);

  void complete(String key, String fingerprint, MovimientoId movimientoId);

  /** Frees a reserved key after a failed attempt so the client can retry with it. */
  void release(String key);
}
