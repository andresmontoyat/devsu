package com.devsu.bank.cuenta.application.usecase;

import com.devsu.bank.cuenta.application.port.inbound.ListCuentasPort;
import com.devsu.bank.cuenta.application.port.outbound.CuentaRepositoryPort;
import com.devsu.bank.cuenta.application.query.CuentasQuery;
import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.domain.model.Cuenta;

public final class ListCuentasUseCase implements ListCuentasPort {

  private final CuentaRepositoryPort cuentas;

  public ListCuentasUseCase(CuentaRepositoryPort cuentas) {
    this.cuentas = cuentas;
  }

  @Override
  public Pagina<Cuenta> execute(CuentasQuery query) {
    return cuentas.findAll(query.clienteId(), query.solicitud());
  }
}
