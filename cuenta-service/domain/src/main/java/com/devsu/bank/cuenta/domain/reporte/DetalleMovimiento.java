package com.devsu.bank.cuenta.domain.reporte;

import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.model.TipoCuenta;
import java.time.Instant;

/**
 * One row of the exercise's "Listado de Movimiento": {@code saldoInicial} is the balance right
 * before the movement and {@code saldoDisponible} the balance right after it.
 */
public record DetalleMovimiento(
    Instant fecha,
    String cliente,
    NumeroCuenta numeroCuenta,
    TipoCuenta tipo,
    Monto saldoInicial,
    boolean estado,
    Monto movimiento,
    Monto saldoDisponible) {}
