package com.devsu.bank.cliente.application.command;

/**
 * Intent to register a client. Pure data: the domain validates it. {@code estado} null = active.
 */
public record CreateClienteCommand(
    String nombre,
    String genero,
    int edad,
    String identificacion,
    String direccion,
    String telefono,
    String contrasena,
    Boolean estado) {

  @Override
  public String toString() {
    return "CreateClienteCommand[identificacion=" + identificacion + "]";
  }
}
