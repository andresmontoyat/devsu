package com.devsu.bank.cliente.domain.model;

import com.devsu.bank.cliente.domain.exception.DatoInvalidoException;
import java.util.UUID;

/** Public business key of a client: stable across services and safe to expose in URLs. */
public record ClienteId(UUID value) {

  private static final String CAMPO = "clienteId";

  public ClienteId {
    Texto.presente(value, CAMPO);
  }

  public static ClienteId generar() {
    return new ClienteId(UUID.randomUUID());
  }

  public static ClienteId of(String texto) {
    Texto.requerido(texto, CAMPO, Integer.MAX_VALUE);
    try {
      return new ClienteId(UUID.fromString(texto.strip()));
    } catch (IllegalArgumentException e) {
      throw new DatoInvalidoException(CAMPO, "no es un UUID valido");
    }
  }
}
