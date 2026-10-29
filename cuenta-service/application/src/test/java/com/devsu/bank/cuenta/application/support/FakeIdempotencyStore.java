package com.devsu.bank.cuenta.application.support;

import com.devsu.bank.cuenta.application.idempotency.IdempotencyOutcome;
import com.devsu.bank.cuenta.application.idempotency.IdempotencyStoreNoDisponibleException;
import com.devsu.bank.cuenta.application.port.outbound.IdempotencyStorePort;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import java.util.HashMap;
import java.util.Map;

/** Same semantics as the Valkey adapter: SET NX reservation, then the completed movement id. */
public final class FakeIdempotencyStore implements IdempotencyStorePort {

  private record Entrada(String fingerprint, MovimientoId movimientoId) {}

  private final Map<String, Entrada> claves = new HashMap<>();
  private boolean caido;

  public void caer() {
    caido = true;
  }

  @Override
  public IdempotencyOutcome reserve(String key, String fingerprint) {
    if (caido) {
      throw new IdempotencyStoreNoDisponibleException();
    }
    Entrada entrada = claves.putIfAbsent(key, new Entrada(fingerprint, null));
    if (entrada == null) {
      return new IdempotencyOutcome.Reserved();
    }
    if (!entrada.fingerprint().equals(fingerprint)) {
      return new IdempotencyOutcome.FingerprintMismatch();
    }
    return entrada.movimientoId() == null
        ? new IdempotencyOutcome.InProgress()
        : new IdempotencyOutcome.Replay(entrada.movimientoId());
  }

  @Override
  public void complete(String key, String fingerprint, MovimientoId movimientoId) {
    claves.put(key, new Entrada(fingerprint, movimientoId));
  }

  @Override
  public void release(String key) {
    claves.remove(key);
  }

  public boolean tiene(String key) {
    return claves.containsKey(key);
  }
}
