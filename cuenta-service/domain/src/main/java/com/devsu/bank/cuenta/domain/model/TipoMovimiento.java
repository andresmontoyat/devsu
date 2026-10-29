package com.devsu.bank.cuenta.domain.model;

import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import com.devsu.bank.cuenta.domain.exception.ValorMovimientoInvalidoException;
import java.util.Arrays;
import java.util.List;

/** F2: the sign of the value decides the type — positive deposits, negative withdraws. */
public enum TipoMovimiento {
  DEPOSITO,
  RETIRO;

  public static TipoMovimiento desde(Monto valor) {
    if (valor.isZero()) {
      throw new ValorMovimientoInvalidoException();
    }
    return valor.isNegative() ? RETIRO : DEPOSITO;
  }

  public static TipoMovimiento desde(String valor) {
    String limpio = Requisitos.texto(valor, "tipoMovimiento");
    return Arrays.stream(values())
        .filter(tipo -> tipo.name().equalsIgnoreCase(limpio))
        .findFirst()
        .orElseThrow(
            () ->
                new DatoInvalidoException(
                    "tipoMovimiento", "valores admitidos: " + List.of(values())));
  }
}
