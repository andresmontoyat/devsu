package com.devsu.bank.cliente.application.usecase;

import com.devsu.bank.cliente.application.event.ClienteOutboxEvents;
import com.devsu.bank.cliente.application.port.inbound.DeleteClientePort;
import com.devsu.bank.cliente.application.port.outbound.ClienteRepositoryPort;
import com.devsu.bank.cliente.application.port.outbound.TransactionPort;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ClienteId;

public final class DeleteClienteUseCase implements DeleteClientePort {

  private final TransactionPort<Void> transaction;
  private final ClienteRepositoryPort repository;
  private final ClienteOutboxEvents events;

  public DeleteClienteUseCase(
      TransactionPort<Void> transaction,
      ClienteRepositoryPort repository,
      ClienteOutboxEvents events) {
    this.transaction = transaction;
    this.repository = repository;
    this.events = events;
  }

  @Override
  public void execute(ClienteId clienteId) {
    transaction.run(() -> eliminar(clienteId));
  }

  private Void eliminar(ClienteId clienteId) {
    Cliente actual = ClienteCambios.obtener(repository, clienteId);
    repository.delete(clienteId);
    events.registrarEliminado(actual);
    return null;
  }
}
