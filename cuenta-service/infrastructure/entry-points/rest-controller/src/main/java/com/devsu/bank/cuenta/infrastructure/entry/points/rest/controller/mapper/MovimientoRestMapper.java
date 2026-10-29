package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.mapper;

import com.devsu.bank.cuenta.domain.model.Movimiento;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto.MovimientoResponse;

public final class MovimientoRestMapper {

  private MovimientoRestMapper() {}

  public static MovimientoResponse toResponse(Movimiento m) {
    return new MovimientoResponse(
        m.id().value().toString(),
        m.fecha(),
        m.numeroCuenta().value(),
        m.tipo().name(),
        m.valor().value(),
        m.saldoAnterior().value(),
        m.saldo().value());
  }
}
