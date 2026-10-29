package com.devsu.bank.cuenta.application.port.inbound;

import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;

@FunctionalInterface
public interface GetCuentaPort {

  Cuenta execute(NumeroCuenta numeroCuenta);
}
