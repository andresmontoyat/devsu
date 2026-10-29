package com.devsu.bank.cuenta.application.usecase;

import com.devsu.bank.cuenta.application.port.inbound.GetCuentaPort;
import com.devsu.bank.cuenta.application.port.outbound.CuentaRepositoryPort;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;

public final class GetCuentaUseCase implements GetCuentaPort {

  private final CuentaRepositoryPort cuentas;

  public GetCuentaUseCase(CuentaRepositoryPort cuentas) {
    this.cuentas = cuentas;
  }

  @Override
  public Cuenta execute(NumeroCuenta numeroCuenta) {
    return Busquedas.cuenta(cuentas, numeroCuenta);
  }
}
