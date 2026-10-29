package com.devsu.bank.cliente.application.port.inbound;

import com.devsu.bank.cliente.application.command.CreateClienteCommand;
import com.devsu.bank.cliente.domain.model.Cliente;

@FunctionalInterface
public interface CreateClientePort {

  Cliente execute(CreateClienteCommand command);
}
