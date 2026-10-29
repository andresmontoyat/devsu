package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** F2: signed value — positive deposits, negative withdraws. The type is optional. */
public record MovimientoRequest(
    @NotBlank String numeroCuenta,
    @NotNull @Digits(integer = 13, fraction = 2) BigDecimal valor,
    String tipoMovimiento) {}
