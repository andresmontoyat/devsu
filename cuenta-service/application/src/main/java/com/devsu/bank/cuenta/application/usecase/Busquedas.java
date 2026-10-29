package com.devsu.bank.cuenta.application.usecase;

import com.devsu.bank.cuenta.application.port.outbound.CuentaRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.MovimientoRepositoryPort;
import com.devsu.bank.cuenta.domain.exception.CuentaNoEncontradaException;
import com.devsu.bank.cuenta.domain.exception.MovimientoNoEncontradoException;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.Movimiento;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;

/** Lookups that turn "absent" into the matching not-found domain exception. */
final class Busquedas {

  private Busquedas() {}

  static Cuenta cuenta(CuentaRepositoryPort cuentas, NumeroCuenta numero) {
    return cuentas.findByNumero(numero).orElseThrow(() -> new CuentaNoEncontradaException(numero));
  }

  static Cuenta cuentaBloqueada(CuentaRepositoryPort cuentas, NumeroCuenta numero) {
    return cuentas
        .findByNumeroForUpdate(numero)
        .orElseThrow(() -> new CuentaNoEncontradaException(numero));
  }

  static Movimiento movimiento(MovimientoRepositoryPort movimientos, MovimientoId id) {
    return movimientos.findById(id).orElseThrow(() -> new MovimientoNoEncontradoException(id));
  }
}
