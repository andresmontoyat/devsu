package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MovimientoUpdateRequest(
    @NotNull @Digits(integer = 13, fraction = 2) BigDecimal valor, String tipoMovimiento) {}
