package com.devsu.bank.cuenta.domain.model;

import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Money as an exact decimal with cents. Values with sub-cent precision are rejected rather than
 * rounded: silently rounding a movement would create or destroy money.
 */
public record Monto(BigDecimal value) implements Comparable<Monto> {

  public static final int ESCALA = 2;
  // Declared before ZERO: static initialisers run in order and the constructor reads them.
  private static final BigDecimal LIMITE = new BigDecimal("10000000000000");
  private static final String CAMPO = "monto";
  public static final Monto ZERO = new Monto(BigDecimal.ZERO);

  public Monto {
    Requisitos.presente(value, CAMPO);
    if (value.stripTrailingZeros().scale() > ESCALA) {
      throw new DatoInvalidoException(CAMPO, "admite como maximo " + ESCALA + " decimales");
    }
    if (value.abs().compareTo(LIMITE) >= 0) {
      throw new DatoInvalidoException(CAMPO, "fuera de rango");
    }
    value = value.setScale(ESCALA, RoundingMode.UNNECESSARY);
  }

  public static Monto of(BigDecimal value) {
    return new Monto(value);
  }

  public static Monto of(String value) {
    try {
      return new Monto(new BigDecimal(Requisitos.texto(value, CAMPO)));
    } catch (NumberFormatException e) {
      throw new DatoInvalidoException(CAMPO, "no es un numero valido");
    }
  }

  public Monto plus(Monto other) {
    return new Monto(value.add(other.value));
  }

  public Monto minus(Monto other) {
    return new Monto(value.subtract(other.value));
  }

  public Monto abs() {
    return new Monto(value.abs());
  }

  public boolean isNegative() {
    return value.signum() < 0;
  }

  public boolean isZero() {
    return value.signum() == 0;
  }

  @Override
  public int compareTo(Monto other) {
    return value.compareTo(other.value);
  }

  @Override
  public String toString() {
    return value.toPlainString();
  }
}
