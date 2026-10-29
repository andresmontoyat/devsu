package com.devsu.bank.cliente.domain.model;

import com.devsu.bank.cliente.domain.exception.DatoInvalidoException;
import java.util.Arrays;
import java.util.List;

public enum Genero {
  MASCULINO,
  FEMENINO,
  OTRO;

  public static Genero desde(String valor) {
    String limpio = Texto.requerido(valor, "genero", Integer.MAX_VALUE);
    return Arrays.stream(values())
        .filter(genero -> genero.name().equalsIgnoreCase(limpio))
        .findFirst()
        .orElseThrow(
            () -> new DatoInvalidoException("genero", "valores admitidos: " + List.of(values())));
  }
}
