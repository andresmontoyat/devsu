package com.devsu.bank.cuenta.application.port.inbound;

import com.devsu.bank.cuenta.application.command.CorrectMovimientoCommand;
import com.devsu.bank.cuenta.domain.model.ResultadoMovimiento;

@FunctionalInterface
public interface CorrectMovimientoPort {

  ResultadoMovimiento execute(CorrectMovimientoCommand command);
}
