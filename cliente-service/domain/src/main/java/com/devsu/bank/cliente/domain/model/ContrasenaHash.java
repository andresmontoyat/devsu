package com.devsu.bank.cliente.domain.model;

/** One-way hash of a client's password; the clear text never reaches the domain model. */
public record ContrasenaHash(String value) {

  public ContrasenaHash {
    Texto.requerido(value, "contrasena", Integer.MAX_VALUE);
  }

  @Override
  public String toString() {
    return "ContrasenaHash[****]";
  }
}
