package com.devsu.bank.cuenta.application.command;

import com.devsu.bank.cuenta.domain.model.MovimientoId;
import java.math.BigDecimal;

/** Replaces the value of an account's latest movement (PUT /movimientos/{id}). */
public record CorrectMovimientoCommand(
    MovimientoId movimientoId, BigDecimal valor, String tipoMovimiento) {}
