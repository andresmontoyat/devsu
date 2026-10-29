package com.devsu.bank.cuenta.domain.model;

import static com.devsu.bank.cuenta.domain.model.CuentaFixtures.FECHA;
import static com.devsu.bank.cuenta.domain.model.CuentaFixtures.MOVIMIENTO_ID;
import static com.devsu.bank.cuenta.domain.model.CuentaFixtures.NUMERO;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import org.junit.jupiter.api.Test;

class MovimientoTest {

  @Test
  void should_reject_a_balance_that_does_not_add_up() {
    assertThatThrownBy(
            () ->
                new Movimiento(
                    MOVIMIENTO_ID,
                    NUMERO,
                    FECHA,
                    TipoMovimiento.DEPOSITO,
                    Monto.of("10"),
                    Monto.of("100"),
                    Monto.of("200")))
        .isInstanceOf(DatoInvalidoException.class)
        .hasMessageContaining("saldo");
  }

  @Test
  void should_reject_a_type_that_contradicts_the_sign() {
    assertThatThrownBy(
            () ->
                new Movimiento(
                    MOVIMIENTO_ID,
                    NUMERO,
                    FECHA,
                    TipoMovimiento.RETIRO,
                    Monto.of("10"),
                    Monto.of("100"),
                    Monto.of("110")))
        .isInstanceOf(DatoInvalidoException.class)
        .hasMessageContaining("tipoMovimiento");
  }

  @Test
  void should_reject_missing_parts() {
    assertThatThrownBy(
            () ->
                new Movimiento(
                    null,
                    NUMERO,
                    FECHA,
                    TipoMovimiento.DEPOSITO,
                    Monto.of("10"),
                    Monto.of("0"),
                    Monto.of("10")))
        .isInstanceOf(DatoInvalidoException.class);
    assertThatThrownBy(
            () ->
                new Movimiento(
                    MOVIMIENTO_ID,
                    NUMERO,
                    null,
                    TipoMovimiento.DEPOSITO,
                    Monto.of("10"),
                    Monto.of("0"),
                    Monto.of("10")))
        .isInstanceOf(DatoInvalidoException.class);
  }
}
