package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.mapper;

import com.devsu.bank.cuenta.application.command.CreateCuentaCommand;
import com.devsu.bank.cuenta.application.command.UpdateCuentaCommand;
import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto.CuentaRequest;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto.CuentaResponse;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto.CuentaUpdateRequest;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto.PageResponse;
import java.util.function.Function;

public final class CuentaRestMapper {

  private CuentaRestMapper() {}

  public static CreateCuentaCommand toCommand(CuentaRequest r) {
    return new CreateCuentaCommand(
        r.numeroCuenta(), r.tipoCuenta(), r.saldoInicial(), r.estado(), r.clienteId());
  }

  public static UpdateCuentaCommand toCommand(String numeroCuenta, CuentaUpdateRequest r) {
    return new UpdateCuentaCommand(
        numeroCuenta, r.tipoCuenta(), r.saldoInicial(), r.estado(), r.clienteId());
  }

  public static CuentaResponse toResponse(Cuenta c) {
    return new CuentaResponse(
        c.getNumeroCuenta().value(),
        c.getTipo().name(),
        c.getSaldoInicial().value(),
        c.getSaldoDisponible().value(),
        c.isActiva(),
        c.getClienteId().value().toString());
  }

  public static <D, R> PageResponse<R> toPage(Pagina<D> pagina, Function<D, R> mapper) {
    PageResponse.PageMetadata metadata =
        new PageResponse.PageMetadata(
            pagina.solicitud().pagina(),
            pagina.solicitud().tamano(),
            pagina.totalElementos(),
            pagina.totalPaginas());
    return new PageResponse<>(pagina.contenido().stream().map(mapper).toList(), metadata);
  }
}
