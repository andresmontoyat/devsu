package com.devsu.bank.cuenta.application.command;

import java.math.BigDecimal;

/** Intent to open an account. {@code estado} null = active. */
public record CreateCuentaCommand(
    String numeroCuenta,
    String tipoCuenta,
    BigDecimal saldoInicial,
    Boolean estado,
    String clienteId) {}
