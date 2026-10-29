package com.devsu.bank.cliente.application.usecase;

import com.devsu.bank.cliente.application.command.ClienteCommandMapper;
import com.devsu.bank.cliente.application.command.CreateClienteCommand;
import com.devsu.bank.cliente.application.event.ClienteOutboxEvents;
import com.devsu.bank.cliente.application.port.inbound.CreateClientePort;
import com.devsu.bank.cliente.application.port.outbound.ClienteRepositoryPort;
import com.devsu.bank.cliente.application.port.outbound.PasswordHasherPort;
import com.devsu.bank.cliente.application.port.outbound.TransactionPort;
import com.devsu.bank.cliente.domain.exception.IdentificacionDuplicadaException;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ClienteId;
import com.devsu.bank.cliente.domain.model.ContrasenaPlana;
import com.devsu.bank.cliente.domain.model.DatosPersona;

public final class CreateClienteUseCase implements CreateClientePort {

  private final TransactionPort<Cliente> transaction;
  private final ClienteRepositoryPort repository;
  private final PasswordHasherPort hasher;
  private final ClienteOutboxEvents events;

  public CreateClienteUseCase(
      TransactionPort<Cliente> transaction,
      ClienteRepositoryPort repository,
      PasswordHasherPort hasher,
      ClienteOutboxEvents events) {
    this.transaction = transaction;
    this.repository = repository;
    this.hasher = hasher;
    this.events = events;
  }

  @Override
  public Cliente execute(CreateClienteCommand command) {
    DatosPersona datos = ClienteCommandMapper.toDatosPersona(command);
    Cliente nuevo =
        Cliente.registrar(
            ClienteId.generar(), datos, hasher.hash(new ContrasenaPlana(command.contrasena())));
    return transaction.run(() -> persistir(ClienteCambios.conEstado(nuevo, command.estado())));
  }

  private Cliente persistir(Cliente cliente) {
    if (repository.existsByIdentificacion(cliente.getIdentificacion())) {
      throw new IdentificacionDuplicadaException(cliente.getIdentificacion());
    }
    Cliente guardado = repository.save(cliente);
    events.registrarCreado(guardado);
    return guardado;
  }
}
