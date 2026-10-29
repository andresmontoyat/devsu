package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto;

import java.math.BigDecimal;

public record CuentaResponse(
    String numeroCuenta,
    String tipoCuenta,
    BigDecimal saldoInicial,
    BigDecimal saldoDisponible,
    boolean estado,
    String clienteId) {}
