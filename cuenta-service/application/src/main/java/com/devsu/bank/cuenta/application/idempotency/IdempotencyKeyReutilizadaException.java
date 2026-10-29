package com.devsu.bank.cuenta.application.idempotency;

public class IdempotencyKeyReutilizadaException extends RuntimeException {

  public IdempotencyKeyReutilizadaException() {
    super("La Idempotency-Key ya se uso con un cuerpo distinto");
  }

  public IdempotencyKeyReutilizadaException(Throwable cause) {
    super("La Idempotency-Key ya se uso con un cuerpo distinto", cause);
  }
}
