package com.devsu.bank.cuenta.domain.exception;

import com.devsu.bank.cuenta.domain.model.NumeroCuenta;

public class CuentaModificadaConcurrentementeException extends DomainException {

  public CuentaModificadaConcurrentementeException(NumeroCuenta numeroCuenta) {
    super("La cuenta " + numeroCuenta.value() + " fue modificada por otra operacion; reintente");
  }
}
