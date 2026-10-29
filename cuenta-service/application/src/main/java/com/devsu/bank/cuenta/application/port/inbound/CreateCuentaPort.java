package com.devsu.bank.cuenta.application.port.inbound;

import com.devsu.bank.cuenta.application.command.CreateCuentaCommand;
import com.devsu.bank.cuenta.domain.model.Cuenta;

@FunctionalInterface
public interface CreateCuentaPort {

  Cuenta execute(CreateCuentaCommand command);
}
