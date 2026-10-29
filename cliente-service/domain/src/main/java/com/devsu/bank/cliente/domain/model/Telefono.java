package com.devsu.bank.cliente.domain.model;

import com.devsu.bank.cliente.domain.exception.DatoInvalidoException;
import java.util.regex.Pattern;

/** Digits only, optional leading '+': the E.164 shape without enforcing a country plan. */
public record Telefono(String value) {

  private static final String CAMPO = "telefono";
  private static final Pattern FORMATO = Pattern.compile("^\\+?[0-9]{7,15}$");

  public Telefono {
    value = Texto.requerido(value, CAMPO, Integer.MAX_VALUE);
    if (!FORMATO.matcher(value).matches()) {
      throw new DatoInvalidoException(CAMPO, "debe tener entre 7 y 15 digitos");
    }
  }
}
