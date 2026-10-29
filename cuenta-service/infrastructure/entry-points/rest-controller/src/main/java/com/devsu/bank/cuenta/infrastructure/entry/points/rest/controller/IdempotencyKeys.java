package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller;

import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import java.util.regex.Pattern;

/** Validates the optional {@code Idempotency-Key} header before it reaches the store. */
final class IdempotencyKeys {

  static final String HEADER = "Idempotency-Key";
  private static final Pattern FORMATO = Pattern.compile("^[\\x21-\\x7E]{1,255}$");

  private IdempotencyKeys() {}

  static String validar(String clave) {
    if (clave != null && !FORMATO.matcher(clave).matches()) {
      throw new DatoInvalidoException(HEADER, "1 a 255 caracteres ASCII visibles");
    }
    return clave;
  }
}
