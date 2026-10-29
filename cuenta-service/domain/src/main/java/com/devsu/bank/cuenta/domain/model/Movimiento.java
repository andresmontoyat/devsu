package com.devsu.bank.cuenta.domain.model;

import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import java.time.Instant;

/**
 * One ledger entry (F2). {@code valor} is signed; {@code saldoAnterior} and {@code saldo} are the
 * balances before and after it, so every entry is self-checking: saldo = saldoAnterior + valor.
 */
public record Movimiento(
    MovimientoId id,
    NumeroCuenta numeroCuenta,
    Instant fecha,
    TipoMovimiento tipo,
    Monto valor,
    Monto saldoAnterior,
    Monto saldo) {

  public Movimiento {
    Requisitos.presente(id, "movimientoId");
    Requisitos.presente(numeroCuenta, "numeroCuenta");
    Requisitos.presente(fecha, "fecha");
    Requisitos.presente(saldoAnterior, "saldoAnterior");
    Requisitos.presente(saldo, "saldo");
    if (Requisitos.presente(tipo, "tipoMovimiento") != TipoMovimiento.desde(valor)) {
      throw new DatoInvalidoException("tipoMovimiento", "no coincide con el signo del valor");
    }
    if (!saldoAnterior.plus(valor).equals(saldo)) {
      throw new DatoInvalidoException("saldo", "no es saldoAnterior + valor");
    }
  }
}
