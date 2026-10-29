package com.devsu.bank.cuenta.domain.model;

import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import java.util.UUID;

/** Key of the client owned by cliente-service; this context only references it. */
public record ClienteId(UUID value) {

  private static final String CAMPO = "clienteId";

  public ClienteId {
    Requisitos.presente(value, CAMPO);
  }

  public static ClienteId of(String texto) {
    String limpio = Requisitos.texto(texto, CAMPO);
    try {
      return new ClienteId(UUID.fromString(limpio));
    } catch (IllegalArgumentException e) {
      throw new DatoInvalidoException(CAMPO, "no es un UUID valido");
    }
  }
}
