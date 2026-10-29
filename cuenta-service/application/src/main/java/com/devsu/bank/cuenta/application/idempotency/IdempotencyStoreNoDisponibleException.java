package com.devsu.bank.cuenta.application.idempotency;

public class IdempotencyStoreNoDisponibleException extends RuntimeException {

  public IdempotencyStoreNoDisponibleException() {
    super("No se puede verificar la Idempotency-Key en este momento; reintente");
  }

  public IdempotencyStoreNoDisponibleException(Throwable cause) {
    super("No se puede verificar la Idempotency-Key en este momento; reintente", cause);
  }
}
