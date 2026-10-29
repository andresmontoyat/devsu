package com.devsu.bank.cuenta.domain.exception;

public class ValorMovimientoInvalidoException extends DomainException {

  public ValorMovimientoInvalidoException() {
    super("El valor del movimiento no puede ser cero");
  }
}
