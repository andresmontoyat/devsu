package com.devsu.bank.cuenta.domain.model;

import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import java.util.regex.Pattern;

/** Business key of an account (the exercise's "numero cuenta"): 6 to 20 digits. */
public record NumeroCuenta(String value) {

  private static final String CAMPO = "numeroCuenta";
  private static final Pattern FORMATO = Pattern.compile("^[0-9]{6,20}$");

  public NumeroCuenta {
    value = Requisitos.texto(value, CAMPO);
    if (!FORMATO.matcher(value).matches()) {
      throw new DatoInvalidoException(CAMPO, "debe tener entre 6 y 20 digitos");
    }
  }
}
