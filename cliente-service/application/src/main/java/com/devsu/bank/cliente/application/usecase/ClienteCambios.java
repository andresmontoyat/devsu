package com.devsu.bank.cliente.application.usecase;

import com.devsu.bank.cliente.application.port.outbound.ClienteRepositoryPort;
import com.devsu.bank.cliente.application.port.outbound.PasswordHasherPort;
import com.devsu.bank.cliente.domain.exception.ClienteNoEncontradoException;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ClienteId;
import com.devsu.bank.cliente.domain.model.ContrasenaHash;
import com.devsu.bank.cliente.domain.model.ContrasenaPlana;
import java.util.Optional;

/** Steps shared by the write use cases of the client aggregate. */
final class ClienteCambios {

  private ClienteCambios() {}

  static Cliente obtener(ClienteRepositoryPort repository, ClienteId clienteId) {
    return repository
        .findById(clienteId)
        .orElseThrow(() -> new ClienteNoEncontradoException(clienteId));
  }

  /** BCrypt is deliberately slow: hash before opening the transaction, not while holding it. */
  static Optional<ContrasenaHash> hashOpcional(PasswordHasherPort hasher, String contrasena) {
    return Optional.ofNullable(contrasena).map(ContrasenaPlana::new).map(hasher::hash);
  }

  static Cliente conContrasena(Cliente cliente, Optional<ContrasenaHash> hash) {
    return hash.map(cliente::cambiarContrasena).orElse(cliente);
  }

  static Cliente conEstado(Cliente cliente, Boolean activo) {
    if (activo == null) {
      return cliente;
    }
    return activo ? cliente.activar() : cliente.desactivar();
  }
}
