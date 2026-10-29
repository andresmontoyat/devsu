package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * F4 "Estado de cuenta". Accounts carry balances and range totals; each movement row uses the exact
 * keys of the exercise's sample JSON ("Fecha", "Numero Cuenta", "Saldo Disponible", …).
 */
public record EstadoCuentaResponse(
    Cliente cliente,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    List<Cuenta> cuentas,
    List<Movimiento> movimientos) {

  public record Cliente(String clienteId, String nombre) {}

  public record Cuenta(
      String numeroCuenta,
      String tipoCuenta,
      BigDecimal saldoInicial,
      BigDecimal saldoDisponible,
      boolean estado,
      BigDecimal totalCreditos,
      BigDecimal totalDebitos) {}

  public record Movimiento(
      @JsonProperty("Fecha") String fecha,
      @JsonProperty("Cliente") String cliente,
      @JsonProperty("Numero Cuenta") String numeroCuenta,
      @JsonProperty("Tipo") String tipo,
      @JsonProperty("Saldo Inicial") BigDecimal saldoInicial,
      @JsonProperty("Estado") boolean estado,
      @JsonProperty("Movimiento") BigDecimal movimiento,
      @JsonProperty("Saldo Disponible") BigDecimal saldoDisponible) {}
}
