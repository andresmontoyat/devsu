package com.devsu.bank.cliente.domain.model;

import com.devsu.bank.cliente.domain.exception.DatoInvalidoException;
import java.util.regex.Pattern;

/** National id or passport: alphanumeric, no separators, so it can be compared as typed. */
public record Identificacion(String value) {

  private static final String CAMPO = "identificacion";
  private static final Pattern FORMATO = Pattern.compile("^[0-9A-Za-z]{5,20}$");

  public Identificacion {
    value = Texto.requerido(value, CAMPO, Integer.MAX_VALUE);
    if (!FORMATO.matcher(value).matches()) {
      throw new DatoInvalidoException(CAMPO, "debe tener entre 5 y 20 caracteres alfanumericos");
    }
  }
}
