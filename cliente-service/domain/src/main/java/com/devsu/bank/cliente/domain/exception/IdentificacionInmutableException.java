package com.devsu.bank.cliente.domain.exception;

public class IdentificacionInmutableException extends DomainException {

  public IdentificacionInmutableException() {
    super("La identificacion de un cliente no puede modificarse");
  }
}
