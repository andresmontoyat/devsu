package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.mapper;

import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.domain.model.Movimiento;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.model.MovimientoPage;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.model.MovimientoResponse;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.model.TipoMovimiento;

/** Maps movements to the models generated from the OpenAPI contract. */
public final class MovimientoRestMapper {

  private MovimientoRestMapper() {}

  public static MovimientoResponse toResponse(Movimiento m) {
    return new MovimientoResponse(
        m.id().value(),
        m.fecha(),
        m.numeroCuenta().value(),
        TipoMovimiento.fromValue(m.tipo().name()),
        m.valor().value(),
        m.saldoAnterior().value(),
        m.saldo().value());
  }

  public static MovimientoPage toPage(Pagina<Movimiento> pagina) {
    return new MovimientoPage(
        pagina.contenido().stream().map(MovimientoRestMapper::toResponse).toList(),
        CuentaRestMapper.metadata(pagina));
  }
}
