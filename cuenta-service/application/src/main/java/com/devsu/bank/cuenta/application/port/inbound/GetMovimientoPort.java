package com.devsu.bank.cuenta.application.port.inbound;

import com.devsu.bank.cuenta.domain.model.Movimiento;
import com.devsu.bank.cuenta.domain.model.MovimientoId;

@FunctionalInterface
public interface GetMovimientoPort {

  Movimiento execute(MovimientoId movimientoId);
}
