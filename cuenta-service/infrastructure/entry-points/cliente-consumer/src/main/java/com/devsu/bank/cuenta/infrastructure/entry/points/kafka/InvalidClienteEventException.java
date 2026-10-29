package com.devsu.bank.cuenta.infrastructure.entry.points.kafka;

/** A message that will never be processable as is: retrying cannot help, it goes to the DLT. */
public class InvalidClienteEventException extends RuntimeException {

  public InvalidClienteEventException(String message) {
    super(message);
  }

  public InvalidClienteEventException(String message, Throwable cause) {
    super(message, cause);
  }
}
