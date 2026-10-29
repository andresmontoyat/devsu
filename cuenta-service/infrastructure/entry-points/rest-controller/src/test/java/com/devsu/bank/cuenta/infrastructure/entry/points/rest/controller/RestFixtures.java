package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller;

import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.model.ResultadoMovimiento;
import com.devsu.bank.cuenta.domain.model.TipoCuenta;
import java.time.Instant;

final class RestFixtures {

  static final ClienteId MARIANELA = ClienteId.of("0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e02");
  static final MovimientoId MOVIMIENTO_ID = MovimientoId.of("5a1d2c3b-4e5f-4a6b-9c7d-8e9f0a1b2c3d");
  static final Instant FECHA = Instant.parse("2022-02-10T15:00:00Z");

  private RestFixtures() {}

  static Cuenta corriente225487() {
    return Cuenta.abrir(
        new NumeroCuenta("225487"), TipoCuenta.CORRIENTE, Monto.of("100"), MARIANELA);
  }

  static ResultadoMovimiento deposito600() {
    return corriente225487().aplicarMovimiento(MOVIMIENTO_ID, Monto.of("600"), FECHA);
  }
}
