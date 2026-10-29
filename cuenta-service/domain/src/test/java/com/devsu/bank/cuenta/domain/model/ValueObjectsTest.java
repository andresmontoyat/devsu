package com.devsu.bank.cuenta.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import com.devsu.bank.cuenta.domain.exception.ValorMovimientoInvalidoException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ValueObjectsTest {

  @Test
  void should_normalise_amounts_to_two_decimals_so_equal_values_are_equal() {
    assertThat(Monto.of("100")).isEqualTo(Monto.of("100.00"));
    assertThat(Monto.of(new BigDecimal("7.5")).value()).isEqualByComparingTo("7.50");
    assertThat(Monto.of("100").value().scale()).isEqualTo(2);
  }

  @Test
  void should_add_subtract_and_compare_amounts() {
    Monto cien = Monto.of("100");

    assertThat(cien.plus(Monto.of("0.50"))).isEqualTo(Monto.of("100.50"));
    assertThat(cien.minus(Monto.of("150"))).isEqualTo(Monto.of("-50"));
    assertThat(Monto.of("-50").isNegative()).isTrue();
    assertThat(Monto.ZERO.isZero()).isTrue();
    assertThat(Monto.of("-2.5").abs()).isEqualTo(Monto.of("2.5"));
    assertThat(cien.toString()).isEqualTo("100.00");
  }

  @ParameterizedTest
  @ValueSource(strings = {"10.005", "abc", "100000000000000"})
  void should_reject_amounts_with_more_than_cents_unparseable_or_out_of_range(String valor) {
    assertThatThrownBy(() -> Monto.of(valor)).isInstanceOf(DatoInvalidoException.class);
  }

  @Test
  void should_reject_a_missing_amount() {
    assertThatThrownBy(() -> Monto.of((BigDecimal) null)).isInstanceOf(DatoInvalidoException.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {"478758", "12345678901234567890"})
  void should_accept_account_numbers_of_six_to_twenty_digits(String numero) {
    assertThat(new NumeroCuenta(" " + numero + " ").value()).isEqualTo(numero);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"12345", "47875A", "123456789012345678901"})
  void should_reject_malformed_account_numbers(String numero) {
    assertThatThrownBy(() -> new NumeroCuenta(numero))
        .isInstanceOf(DatoInvalidoException.class)
        .hasMessageContaining("numeroCuenta");
  }

  @ParameterizedTest
  @CsvSource({"Ahorros, AHORROS", "CORRIENTE, CORRIENTE", "corriente, CORRIENTE"})
  void should_resolve_the_account_type_ignoring_case(String texto, TipoCuenta tipo) {
    assertThat(TipoCuenta.desde(texto)).isEqualTo(tipo);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"Plazo fijo"})
  void should_reject_unknown_account_types(String texto) {
    assertThatThrownBy(() -> TipoCuenta.desde(texto))
        .isInstanceOf(DatoInvalidoException.class)
        .hasMessageContaining("tipoCuenta");
  }

  @Test
  void should_derive_the_movement_type_from_the_sign() {
    assertThat(TipoMovimiento.desde(Monto.of("1"))).isEqualTo(TipoMovimiento.DEPOSITO);
    assertThat(TipoMovimiento.desde(Monto.of("-1"))).isEqualTo(TipoMovimiento.RETIRO);
  }

  @Test
  void should_reject_deriving_a_movement_type_from_zero() {
    assertThatThrownBy(() -> TipoMovimiento.desde(Monto.ZERO))
        .isInstanceOf(ValorMovimientoInvalidoException.class);
  }

  @Test
  void should_resolve_a_declared_movement_type_ignoring_case() {
    assertThat(TipoMovimiento.desde("retiro")).isEqualTo(TipoMovimiento.RETIRO);
    assertThatThrownBy(() -> TipoMovimiento.desde("transferencia"))
        .isInstanceOf(DatoInvalidoException.class);
  }

  @Test
  void should_parse_ids_and_reject_invalid_text() {
    String uuid = "0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e01";

    assertThat(ClienteId.of(uuid).value()).hasToString(uuid);
    assertThat(MovimientoId.of(uuid).value()).hasToString(uuid);
    assertThat(MovimientoId.generar()).isNotEqualTo(MovimientoId.generar());
    assertThatThrownBy(() -> ClienteId.of("x")).isInstanceOf(DatoInvalidoException.class);
    assertThatThrownBy(() -> MovimientoId.of(null)).isInstanceOf(DatoInvalidoException.class);
    assertThatThrownBy(() -> new ClienteId(null)).isInstanceOf(DatoInvalidoException.class);
  }
}
