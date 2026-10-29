package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** PUT body: the number comes from the path. */
public record CuentaUpdateRequest(
    @NotBlank String tipoCuenta,
    @NotNull @DecimalMin("0.00") @Digits(integer = 13, fraction = 2) BigDecimal saldoInicial,
    @NotNull Boolean estado,
    @NotBlank String clienteId) {}
