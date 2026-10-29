package com.devsu.bank.cuenta.application.command;

import java.math.BigDecimal;

/**
 * F2: signed value — positive deposits, negative withdraws. {@code tipoMovimiento} is optional and,
 * when sent, must agree with the sign. {@code idempotencyKey} is the optional request header.
 */
public record RegisterMovimientoCommand(
    String numeroCuenta, BigDecimal valor, String tipoMovimiento, String idempotencyKey) {

  /** Identifies the business intent, so a reused key with another body can be detected. */
  public String fingerprint() {
    return numeroCuenta + "|" + valor.stripTrailingZeros().toPlainString() + "|" + tipoMovimiento;
  }
}
