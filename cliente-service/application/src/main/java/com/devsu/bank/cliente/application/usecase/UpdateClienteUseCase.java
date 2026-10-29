package com.devsu.bank.cliente.application.usecase;

import com.devsu.bank.cliente.application.command.ClienteCommandMapper;
import com.devsu.bank.cliente.application.command.UpdateClienteCommand;
import com.devsu.bank.cliente.application.event.ClienteOutboxEvents;
import com.devsu.bank.cliente.application.port.inbound.UpdateClientePort;
import com.devsu.bank.cliente.application.port.outbound.ClienteRepositoryPort;
import com.devsu.bank.cliente.application.port.outbound.PasswordHasherPort;
import com.devsu.bank.cliente.application.port.outbound.TransactionPort;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ContrasenaHash;
import com.devsu.bank.cliente.domain.model.DatosPersona;
import java.util.Optional;

public final class UpdateClienteUseCase implements UpdateClientePort {

  private final TransactionPort<Cliente> transaction;
  private final ClienteRepositoryPort repository;
  private final PasswordHasherPort hasher;
  private final ClienteOutboxEvents events;

  public UpdateClienteUseCase(
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
  public Cliente execute(UpdateClienteCommand command) {
    DatosPersona datos = ClienteCommandMapper.toDatosPersona(command);
    Optional<ContrasenaHash> hash = ClienteCambios.hashOpcional(hasher, command.contrasena());
    return transaction.run(() -> actualizar(command, datos, hash));
  }

  private Cliente actualizar(
      UpdateClienteCommand command, DatosPersona datos, Optional<ContrasenaHash> hash) {
    Cliente actual = ClienteCambios.obtener(repository, command.clienteId());
    Cliente cambiado = ClienteCambios.conContrasena(actual.actualizarDatos(datos), hash);
    Cliente guardado = repository.save(ClienteCambios.conEstado(cambiado, command.estado()));
    events.registrarActualizado(guardado);
    return guardado;
  }
}
