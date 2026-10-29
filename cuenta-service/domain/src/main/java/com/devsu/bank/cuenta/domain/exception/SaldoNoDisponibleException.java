package com.devsu.bank.cuenta.domain.exception;

/** F3: a movement that would leave the account below zero. The message is fixed by the exercise. */
public class SaldoNoDisponibleException extends DomainException {

  public static final String MENSAJE = "Saldo no disponible";

  public SaldoNoDisponibleException() {
    super(MENSAJE);
  }
}
