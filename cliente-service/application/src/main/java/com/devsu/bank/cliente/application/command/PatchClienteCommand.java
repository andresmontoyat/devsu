package com.devsu.bank.cliente.application.command;

import com.devsu.bank.cliente.domain.model.ClienteId;

/**
 * Partial update (JSON Merge Patch). A null field means "leave unchanged"; the identification is
 * not patchable.
 */
public record PatchClienteCommand(
    ClienteId clienteId,
    String nombre,
    String genero,
    Integer edad,
    String direccion,
    String telefono,
    String contrasena,
    Boolean estado) {

  public static Builder builder(ClienteId clienteId) {
    return new Builder(clienteId);
  }

  @Override
  public String toString() {
    return "PatchClienteCommand[clienteId=" + clienteId.value() + "]";
  }

  public static final class Builder {
    private final ClienteId clienteId;
    private String nombre;
    private String genero;
    private Integer edad;
    private String direccion;
    private String telefono;
    private String contrasena;
    private Boolean estado;

    private Builder(ClienteId clienteId) {
      this.clienteId = clienteId;
    }

    public Builder nombre(String nombre) {
      this.nombre = nombre;
      return this;
    }

    public Builder genero(String genero) {
      this.genero = genero;
      return this;
    }

    public Builder edad(Integer edad) {
      this.edad = edad;
      return this;
    }

    public Builder direccion(String direccion) {
      this.direccion = direccion;
      return this;
    }

    public Builder telefono(String telefono) {
      this.telefono = telefono;
      return this;
    }

    public Builder contrasena(String contrasena) {
      this.contrasena = contrasena;
      return this;
    }

    public Builder estado(Boolean estado) {
      this.estado = estado;
      return this;
    }

    public PatchClienteCommand build() {
      return new PatchClienteCommand(
          clienteId, nombre, genero, edad, direccion, telefono, contrasena, estado);
    }
  }
}
