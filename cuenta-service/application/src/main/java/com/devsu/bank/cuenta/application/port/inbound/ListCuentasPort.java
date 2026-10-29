package com.devsu.bank.cuenta.application.port.inbound;

import com.devsu.bank.cuenta.application.query.CuentasQuery;
import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.domain.model.Cuenta;

@FunctionalInterface
public interface ListCuentasPort {

  Pagina<Cuenta> execute(CuentasQuery query);
}
