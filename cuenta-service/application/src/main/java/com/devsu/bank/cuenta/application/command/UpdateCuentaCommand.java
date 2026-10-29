package com.devsu.bank.cuenta.application.command;

import java.math.BigDecimal;

/**
 * Full replacement (PUT). The owner and, once there are movements, the initial balance are fixed.
 */
public record UpdateCuentaCommand(
    String numeroCuenta,
    String tipoCuenta,
    BigDecimal saldoInicial,
    boolean estado,
    String clienteId) {}
