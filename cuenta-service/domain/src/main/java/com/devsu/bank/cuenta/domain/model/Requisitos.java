package com.devsu.bank.cuenta.domain.model;

import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;

/** Shared presence guards for the value objects of this package. */
final class Requisitos {

  private Requisitos() {}

  static <T> T presente(T valor, String campo) {
    if (valor == null) {
      throw new DatoInvalidoException(campo, "es obligatorio");
    }
    return valor;
  }

  static String texto(String valor, String campo) {
    if (valor == null || valor.isBlank()) {
      throw new DatoInvalidoException(campo, "es obligatorio");
    }
    return valor.strip();
  }
}
