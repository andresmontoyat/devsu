package com.devsu.bank.cuenta.application.port.inbound;

import com.devsu.bank.cuenta.application.command.UpdateCuentaCommand;
import com.devsu.bank.cuenta.domain.model.Cuenta;

@FunctionalInterface
public interface UpdateCuentaPort {

  Cuenta execute(UpdateCuentaCommand command);
}
