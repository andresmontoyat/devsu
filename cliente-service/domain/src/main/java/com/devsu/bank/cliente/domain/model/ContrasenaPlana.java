package com.devsu.bank.cliente.domain.model;

import com.devsu.bank.cliente.domain.exception.DatoInvalidoException;

/**
 * Clear-text password as received, held only until it is hashed. The upper bound is BCrypt's
 * 72-byte input limit: anything longer would be silently truncated.
 */
public record ContrasenaPlana(String value) {

  public static final int LONGITUD_MIN = 4;
  public static final int LONGITUD_MAX = 72;
  private static final String CAMPO = "contrasena";

  public ContrasenaPlana {
    Texto.requerido(value, CAMPO, LONGITUD_MAX);
    if (value.length() < LONGITUD_MIN) {
      throw new DatoInvalidoException(CAMPO, "debe tener al menos " + LONGITUD_MIN + " caracteres");
    }
  }

  @Override
  public String toString() {
    return "ContrasenaPlana[****]";
  }
}
