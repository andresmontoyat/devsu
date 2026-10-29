package com.devsu.bank.cliente.application.port.inbound;

import com.devsu.bank.cliente.application.command.PatchClienteCommand;
import com.devsu.bank.cliente.domain.model.Cliente;

@FunctionalInterface
public interface PatchClientePort {

  Cliente execute(PatchClienteCommand command);
}
