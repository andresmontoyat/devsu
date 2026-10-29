package com.devsu.bank.cuenta.application.idempotency;

public class IdempotencyKeyEnCursoException extends RuntimeException {

  public IdempotencyKeyEnCursoException() {
    super("Hay otra peticion en curso con la misma Idempotency-Key");
  }

  public IdempotencyKeyEnCursoException(Throwable cause) {
    super("Hay otra peticion en curso con la misma Idempotency-Key", cause);
  }
}
