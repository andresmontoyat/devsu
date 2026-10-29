package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record CuentaRequest(
    @NotBlank @Pattern(regexp = "\\d{6,20}") String numeroCuenta,
    @NotBlank String tipoCuenta,
    @NotNull @DecimalMin("0.00") @Digits(integer = 13, fraction = 2) BigDecimal saldoInicial,
    Boolean estado,
    @NotBlank String clienteId) {}
