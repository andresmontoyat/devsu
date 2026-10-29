package com.devsu.bank.cuenta.domain.reporte;

import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.model.TipoCuenta;

/** One account of the statement with its current balance and the totals moved in the range. */
public record ResumenCuenta(
    NumeroCuenta numeroCuenta,
    TipoCuenta tipo,
    Monto saldoInicial,
    Monto saldoDisponible,
    boolean activa,
    Monto totalCreditos,
    Monto totalDebitos) {}
