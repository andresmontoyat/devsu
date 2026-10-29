package com.devsu.bank.cuenta.application.port.inbound;

import com.devsu.bank.cuenta.application.command.PatchCuentaCommand;
import com.devsu.bank.cuenta.domain.model.Cuenta;

@FunctionalInterface
public interface PatchCuentaPort {

  Cuenta execute(PatchCuentaCommand command);
}
