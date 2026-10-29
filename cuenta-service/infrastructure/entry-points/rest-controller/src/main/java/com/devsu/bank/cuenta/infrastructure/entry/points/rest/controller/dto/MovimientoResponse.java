package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** {@code saldoInicial} is the balance before the movement, {@code saldoDisponible} after it. */
public record MovimientoResponse(
    String movimientoId,
    Instant fecha,
    String numeroCuenta,
    String tipoMovimiento,
    BigDecimal valor,
    BigDecimal saldoInicial,
    BigDecimal saldoDisponible) {}
