package com.devsu.bank.cuenta.domain.exception;

import com.devsu.bank.cuenta.domain.model.NumeroCuenta;

public class CuentaInactivaException extends DomainException {

  public CuentaInactivaException(NumeroCuenta numeroCuenta) {
    super("La cuenta " + numeroCuenta.value() + " esta inactiva");
  }
}
