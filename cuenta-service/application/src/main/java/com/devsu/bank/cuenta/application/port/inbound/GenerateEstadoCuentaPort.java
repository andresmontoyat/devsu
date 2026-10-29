package com.devsu.bank.cuenta.application.port.inbound;

import com.devsu.bank.cuenta.application.query.EstadoCuentaQuery;
import com.devsu.bank.cuenta.domain.reporte.EstadoCuenta;

@FunctionalInterface
public interface GenerateEstadoCuentaPort {

  EstadoCuenta execute(EstadoCuentaQuery query);
}
