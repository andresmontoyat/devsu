package com.devsu.bank.cuenta.domain.exception;

import com.devsu.bank.cuenta.domain.model.NumeroCuenta;

public class NumeroCuentaDuplicadoException extends DomainException {

  public NumeroCuentaDuplicadoException(NumeroCuenta numeroCuenta) {
    super("Ya existe la cuenta " + numeroCuenta.value());
  }
}
