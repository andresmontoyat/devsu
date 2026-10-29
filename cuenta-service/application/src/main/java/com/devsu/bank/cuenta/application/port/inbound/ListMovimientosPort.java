package com.devsu.bank.cuenta.application.port.inbound;

import com.devsu.bank.cuenta.application.query.MovimientosQuery;
import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.domain.model.Movimiento;

@FunctionalInterface
public interface ListMovimientosPort {

  Pagina<Movimiento> execute(MovimientosQuery query);
}
