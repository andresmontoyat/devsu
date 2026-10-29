package com.devsu.bank.cliente.application.port.inbound;

import com.devsu.bank.cliente.domain.model.ClienteId;

@FunctionalInterface
public interface DeleteClientePort {

  void execute(ClienteId clienteId);
}
