package com.devsu.bank.cuenta.domain.model;

import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import java.util.UUID;

/** Unique key of a movement in the ledger. */
public record MovimientoId(UUID value) {

  private static final String CAMPO = "movimientoId";

  public MovimientoId {
    Requisitos.presente(value, CAMPO);
  }

  public static MovimientoId of(String texto) {
    String limpio = Requisitos.texto(texto, CAMPO);
    try {
      return new MovimientoId(UUID.fromString(limpio));
    } catch (IllegalArgumentException e) {
      throw new DatoInvalidoException(CAMPO, "no es un UUID valido");
    }
  }

  public static MovimientoId generar() {
    return new MovimientoId(UUID.randomUUID());
  }
}
