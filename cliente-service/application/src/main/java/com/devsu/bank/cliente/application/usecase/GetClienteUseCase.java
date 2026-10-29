package com.devsu.bank.cliente.application.usecase;

import com.devsu.bank.cliente.application.port.inbound.GetClientePort;
import com.devsu.bank.cliente.application.port.outbound.ClienteRepositoryPort;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ClienteId;

public final class GetClienteUseCase implements GetClientePort {

  private final ClienteRepositoryPort repository;

  public GetClienteUseCase(ClienteRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  public Cliente execute(ClienteId clienteId) {
    return ClienteCambios.obtener(repository, clienteId);
  }
}
