package com.devsu.bank.cliente.application.usecase;

import com.devsu.bank.cliente.application.command.ClienteCommandMapper;
import com.devsu.bank.cliente.application.command.PatchClienteCommand;
import com.devsu.bank.cliente.application.event.ClienteOutboxEvents;
import com.devsu.bank.cliente.application.port.inbound.PatchClientePort;
import com.devsu.bank.cliente.application.port.outbound.ClienteRepositoryPort;
import com.devsu.bank.cliente.application.port.outbound.PasswordHasherPort;
import com.devsu.bank.cliente.application.port.outbound.TransactionPort;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ContrasenaHash;
import java.util.Optional;

public final class PatchClienteUseCase implements PatchClientePort {

  private final TransactionPort<Cliente> transaction;
  private final ClienteRepositoryPort repository;
  private final PasswordHasherPort hasher;
  private final ClienteOutboxEvents events;

  public PatchClienteUseCase(
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
  public Cliente execute(PatchClienteCommand command) {
    Optional<ContrasenaHash> hash = ClienteCambios.hashOpcional(hasher, command.contrasena());
    return transaction.run(() -> aplicar(command, hash));
  }

  private Cliente aplicar(PatchClienteCommand command, Optional<ContrasenaHash> hash) {
    Cliente actual = ClienteCambios.obtener(repository, command.clienteId());
    Cliente cambiado =
        actual.actualizarDatos(ClienteCommandMapper.merge(actual.getDatos(), command));
    Cliente conContrasena = ClienteCambios.conContrasena(cambiado, hash);
    Cliente guardado = repository.save(ClienteCambios.conEstado(conContrasena, command.estado()));
    events.registrarActualizado(guardado);
    return guardado;
  }
}
