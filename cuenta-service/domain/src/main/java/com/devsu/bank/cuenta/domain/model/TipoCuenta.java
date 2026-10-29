package com.devsu.bank.cuenta.domain.model;

import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import java.util.Arrays;
import java.util.List;

public enum TipoCuenta {
  AHORROS,
  CORRIENTE;

  public static TipoCuenta desde(String valor) {
    String limpio = Requisitos.texto(valor, "tipoCuenta");
    return Arrays.stream(values())
        .filter(tipo -> tipo.name().equalsIgnoreCase(limpio))
        .findFirst()
        .orElseThrow(
            () ->
                new DatoInvalidoException("tipoCuenta", "valores admitidos: " + List.of(values())));
  }
}
