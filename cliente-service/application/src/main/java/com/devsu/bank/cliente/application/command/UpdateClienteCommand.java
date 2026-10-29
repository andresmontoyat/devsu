package com.devsu.bank.cliente.application.command;

import com.devsu.bank.cliente.domain.model.ClienteId;

/** Full replacement (PUT). {@code contrasena} null keeps the current password. */
public record UpdateClienteCommand(
    ClienteId clienteId,
    String nombre,
    String genero,
    int edad,
    String identificacion,
    String direccion,
    String telefono,
    String contrasena,
    boolean estado) {

  @Override
  public String toString() {
    return "UpdateClienteCommand[clienteId=" + clienteId.value() + "]";
  }
}
