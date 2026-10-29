package com.devsu.bank.cuenta.application.port.inbound;

import com.devsu.bank.cuenta.application.command.RegisterMovimientoCommand;
import com.devsu.bank.cuenta.domain.model.ResultadoMovimiento;

@FunctionalInterface
public interface RegisterMovimientoPort {

  ResultadoMovimiento execute(RegisterMovimientoCommand command);
}
