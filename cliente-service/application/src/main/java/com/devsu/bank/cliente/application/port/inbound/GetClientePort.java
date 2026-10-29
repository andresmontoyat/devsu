package com.devsu.bank.cliente.application.port.inbound;

import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ClienteId;

@FunctionalInterface
public interface GetClientePort {

  Cliente execute(ClienteId clienteId);
}
