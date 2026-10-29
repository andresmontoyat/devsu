package com.devsu.bank.cliente.domain.model;

import com.devsu.bank.cliente.domain.exception.DatoInvalidoException;

/** Shared guard for mandatory, bounded free-text attributes. */
final class Texto {

  private Texto() {}

  static String requerido(String valor, String campo, int longitudMaxima) {
    if (valor == null || valor.isBlank()) {
      throw new DatoInvalidoException(campo, "es obligatorio");
    }
    String limpio = valor.strip();
    if (limpio.length() > longitudMaxima) {
      throw new DatoInvalidoException(campo, "supera " + longitudMaxima + " caracteres");
    }
    return limpio;
  }

  static <T> T presente(T valor, String campo) {
    if (valor == null) {
      throw new DatoInvalidoException(campo, "es obligatorio");
    }
    return valor;
  }
}
