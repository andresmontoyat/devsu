package com.devsu.bank.cuenta.domain.exception;

import com.devsu.bank.cuenta.domain.model.NumeroCuenta;

/** Once a movement exists, the ledger starts from the initial balance: it can no longer change. */
public class SaldoInicialNoModificableException extends DomainException {

  public SaldoInicialNoModificableException(NumeroCuenta numeroCuenta) {
    super("La cuenta " + numeroCuenta.value() + " ya tiene movimientos; su saldo inicial es fijo");
  }
}
