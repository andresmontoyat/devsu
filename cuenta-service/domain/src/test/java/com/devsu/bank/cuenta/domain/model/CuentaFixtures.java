package com.devsu.bank.cuenta.domain.model;

import java.time.Instant;
import java.util.UUID;

public final class CuentaFixtures {

  public static final NumeroCuenta NUMERO = new NumeroCuenta("478758");
  public static final ClienteId JOSE_LEMA =
      new ClienteId(UUID.fromString("0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e01"));
  public static final Instant FECHA = Instant.parse("2022-02-10T15:00:00Z");
  public static final MovimientoId MOVIMIENTO_ID =
      new MovimientoId(UUID.fromString("5a1d2c3b-4e5f-4a6b-9c7d-8e9f0a1b2c3d"));

  private CuentaFixtures() {}

  public static Cuenta ahorrosCon(String saldo) {
    return Cuenta.abrir(NUMERO, TipoCuenta.AHORROS, Monto.of(saldo), JOSE_LEMA);
  }
}
