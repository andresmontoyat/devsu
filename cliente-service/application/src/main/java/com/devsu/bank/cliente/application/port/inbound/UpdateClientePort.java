package com.devsu.bank.cliente.application.port.inbound;

import com.devsu.bank.cliente.application.command.UpdateClienteCommand;
import com.devsu.bank.cliente.domain.model.Cliente;

@FunctionalInterface
public interface UpdateClientePort {

  Cliente execute(UpdateClienteCommand command);
}
