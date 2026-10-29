package com.devsu.bank.cuenta.domain.exception;

public class RangoFechasInvalidoException extends DomainException {

  public RangoFechasInvalidoException(String detalle) {
    super("Rango de fechas invalido: " + detalle);
  }
}
