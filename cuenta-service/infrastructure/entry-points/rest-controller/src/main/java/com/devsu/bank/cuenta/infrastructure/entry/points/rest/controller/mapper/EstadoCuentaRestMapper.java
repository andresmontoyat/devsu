package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.mapper;

import com.devsu.bank.cuenta.domain.model.TipoCuenta;
import com.devsu.bank.cuenta.domain.reporte.DetalleMovimiento;
import com.devsu.bank.cuenta.domain.reporte.EstadoCuenta;
import com.devsu.bank.cuenta.domain.reporte.ResumenCuenta;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto.EstadoCuentaResponse;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Domain statement → the exercise's JSON shape (dates as d/M/yyyy in the bank's zone). */
public final class EstadoCuentaRestMapper {

  private static final String FORMATO_FECHA = "d/M/yyyy";

  private EstadoCuentaRestMapper() {}

  public static EstadoCuentaResponse toResponse(EstadoCuenta estado, ZoneId zona) {
    DateTimeFormatter formato = DateTimeFormatter.ofPattern(FORMATO_FECHA).withZone(zona);
    return new EstadoCuentaResponse(
        new EstadoCuentaResponse.Cliente(
            estado.cliente().clienteId().value().toString(), estado.cliente().nombre()),
        estado.rango().desde(),
        estado.rango().hasta(),
        estado.cuentas().stream().map(EstadoCuentaRestMapper::cuenta).toList(),
        estado.movimientos().stream().map(m -> movimiento(m, formato)).toList());
  }

  private static EstadoCuentaResponse.Cuenta cuenta(ResumenCuenta r) {
    return new EstadoCuentaResponse.Cuenta(
        r.numeroCuenta().value(),
        r.tipo().name(),
        r.saldoInicial().value(),
        r.saldoDisponible().value(),
        r.activa(),
        r.totalCreditos().value(),
        r.totalDebitos().value());
  }

  private static EstadoCuentaResponse.Movimiento movimiento(
      DetalleMovimiento d, DateTimeFormatter formato) {
    return new EstadoCuentaResponse.Movimiento(
        formato.format(d.fecha()),
        d.cliente(),
        d.numeroCuenta().value(),
        legible(d.tipo()),
        d.saldoInicial().value(),
        d.estado(),
        d.movimiento().value(),
        d.saldoDisponible().value());
  }

  /** "Ahorros" / "Corriente", as written in the exercise. */
  private static String legible(TipoCuenta tipo) {
    String nombre = tipo.name().toLowerCase(Locale.ROOT);
    return Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1);
  }
}
