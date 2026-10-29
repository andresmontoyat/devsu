package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.mapper;

import com.devsu.bank.cuenta.application.command.CreateCuentaCommand;
import com.devsu.bank.cuenta.application.command.UpdateCuentaCommand;
import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.model.CuentaPage;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.model.CuentaRequest;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.model.CuentaResponse;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.model.CuentaUpdateRequest;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.model.PageMetadata;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.model.TipoCuenta;

/** Maps the models generated from the OpenAPI contract to commands and back. */
public final class CuentaRestMapper {

  private CuentaRestMapper() {}

  public static CreateCuentaCommand toCommand(CuentaRequest r) {
    return new CreateCuentaCommand(
        r.getNumeroCuenta(),
        r.getTipoCuenta(),
        r.getSaldoInicial(),
        r.getEstado(),
        r.getClienteId());
  }

  public static UpdateCuentaCommand toCommand(String numeroCuenta, CuentaUpdateRequest r) {
    return new UpdateCuentaCommand(
        numeroCuenta, r.getTipoCuenta(), r.getSaldoInicial(), r.getEstado(), r.getClienteId());
  }

  public static CuentaResponse toResponse(Cuenta c) {
    return new CuentaResponse(
        c.getNumeroCuenta().value(),
        TipoCuenta.fromValue(c.getTipo().name()),
        c.getSaldoInicial().value(),
        c.getSaldoDisponible().value(),
        c.isActiva(),
        c.getClienteId().value());
  }

  public static CuentaPage toPage(Pagina<Cuenta> pagina) {
    return new CuentaPage(
        pagina.contenido().stream().map(CuentaRestMapper::toResponse).toList(), metadata(pagina));
  }

  public static PageMetadata metadata(Pagina<?> pagina) {
    return new PageMetadata(
        pagina.solicitud().pagina(),
        pagina.solicitud().tamano(),
        pagina.totalElementos(),
        (long) pagina.totalPaginas());
  }
}
