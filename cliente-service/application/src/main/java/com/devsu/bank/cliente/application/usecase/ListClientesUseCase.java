package com.devsu.bank.cliente.application.usecase;

import com.devsu.bank.cliente.application.port.inbound.ListClientesPort;
import com.devsu.bank.cliente.application.port.outbound.ClienteRepositoryPort;
import com.devsu.bank.cliente.application.query.Pagina;
import com.devsu.bank.cliente.application.query.PaginaSolicitud;
import com.devsu.bank.cliente.domain.model.Cliente;

public final class ListClientesUseCase implements ListClientesPort {

  private final ClienteRepositoryPort repository;

  public ListClientesUseCase(ClienteRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  public Pagina<Cliente> execute(PaginaSolicitud solicitud) {
    return repository.findAll(solicitud);
  }
}
