package com.devsu.bank.cuenta.domain.exception;

import com.devsu.bank.cuenta.domain.model.NumeroCuenta;

public class CuentaNoEncontradaException extends DomainException {

  public CuentaNoEncontradaException(NumeroCuenta numeroCuenta) {
    super("No existe la cuenta " + numeroCuenta.value());
  }
}
