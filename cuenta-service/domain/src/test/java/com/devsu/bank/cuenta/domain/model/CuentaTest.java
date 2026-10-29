package com.devsu.bank.cuenta.domain.model;

import static com.devsu.bank.cuenta.domain.model.CuentaFixtures.FECHA;
import static com.devsu.bank.cuenta.domain.model.CuentaFixtures.JOSE_LEMA;
import static com.devsu.bank.cuenta.domain.model.CuentaFixtures.MOVIMIENTO_ID;
import static com.devsu.bank.cuenta.domain.model.CuentaFixtures.NUMERO;
import static com.devsu.bank.cuenta.domain.model.CuentaFixtures.ahorrosCon;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cuenta.domain.exception.CuentaInactivaException;
import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import com.devsu.bank.cuenta.domain.exception.MovimientoNoModificableException;
import com.devsu.bank.cuenta.domain.exception.SaldoNoDisponibleException;
import com.devsu.bank.cuenta.domain.exception.ValorMovimientoInvalidoException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CuentaTest {

  private static final long VERSION = 7L;

  @Test
  void should_open_active_with_the_initial_balance_available() {
    Cuenta cuenta = ahorrosCon("2000");

    assertThat(cuenta.getNumeroCuenta()).isEqualTo(NUMERO);
    assertThat(cuenta.getTipo()).isEqualTo(TipoCuenta.AHORROS);
    assertThat(cuenta.getSaldoInicial()).isEqualTo(Monto.of("2000"));
    assertThat(cuenta.getSaldoDisponible()).isEqualTo(Monto.of("2000"));
    assertThat(cuenta.getClienteId()).isEqualTo(JOSE_LEMA);
    assertThat(cuenta.isActiva()).isTrue();
    assertThat(cuenta.getVersion()).isZero();
  }

  @Test
  void should_reject_a_negative_initial_balance() {
    assertThatThrownBy(() -> ahorrosCon("-1"))
        .isInstanceOf(DatoInvalidoException.class)
        .hasMessageContaining("saldoInicial");
  }

  @Test
  void should_reject_opening_without_mandatory_parts() {
    assertThatThrownBy(() -> Cuenta.abrir(null, TipoCuenta.AHORROS, Monto.of("1"), JOSE_LEMA))
        .isInstanceOf(DatoInvalidoException.class);
    assertThatThrownBy(() -> Cuenta.abrir(NUMERO, null, Monto.of("1"), JOSE_LEMA))
        .isInstanceOf(DatoInvalidoException.class);
    assertThatThrownBy(() -> Cuenta.abrir(NUMERO, TipoCuenta.AHORROS, null, JOSE_LEMA))
        .isInstanceOf(DatoInvalidoException.class);
    assertThatThrownBy(() -> Cuenta.abrir(NUMERO, TipoCuenta.AHORROS, Monto.of("1"), null))
        .isInstanceOf(DatoInvalidoException.class);
  }

  @Test
  void should_increase_the_balance_and_record_a_deposit_when_the_value_is_positive() {
    ResultadoMovimiento resultado =
        ahorrosCon("100").aplicarMovimiento(MOVIMIENTO_ID, Monto.of("600"), FECHA);

    assertThat(resultado.cuenta().getSaldoDisponible()).isEqualTo(Monto.of("700"));
    assertThat(resultado.movimiento())
        .isEqualTo(
            new Movimiento(
                MOVIMIENTO_ID,
                NUMERO,
                FECHA,
                TipoMovimiento.DEPOSITO,
                Monto.of("600"),
                Monto.of("100"),
                Monto.of("700")));
  }

  @Test
  void should_decrease_the_balance_and_record_a_withdrawal_when_the_value_is_negative() {
    ResultadoMovimiento resultado =
        ahorrosCon("2000").aplicarMovimiento(MOVIMIENTO_ID, Monto.of("-575"), FECHA);

    assertThat(resultado.cuenta().getSaldoDisponible()).isEqualTo(Monto.of("1425"));
    assertThat(resultado.movimiento().tipo()).isEqualTo(TipoMovimiento.RETIRO);
    assertThat(resultado.movimiento().saldoAnterior()).isEqualTo(Monto.of("2000"));
  }

  @Test
  void should_allow_withdrawing_the_whole_balance() {
    ResultadoMovimiento resultado =
        ahorrosCon("540").aplicarMovimiento(MOVIMIENTO_ID, Monto.of("-540"), FECHA);

    assertThat(resultado.cuenta().getSaldoDisponible()).isEqualTo(Monto.ZERO);
  }

  @Test
  void should_fail_with_saldo_no_disponible_when_the_withdrawal_exceeds_the_balance() {
    Cuenta cuenta = ahorrosCon("0");

    assertThatThrownBy(() -> cuenta.aplicarMovimiento(MOVIMIENTO_ID, Monto.of("-0.01"), FECHA))
        .isInstanceOf(SaldoNoDisponibleException.class)
        .hasMessage("Saldo no disponible");
  }

  @Test
  void should_reject_a_movement_of_zero() {
    Cuenta cuenta = ahorrosCon("100");

    assertThatThrownBy(() -> cuenta.aplicarMovimiento(MOVIMIENTO_ID, Monto.ZERO, FECHA))
        .isInstanceOf(ValorMovimientoInvalidoException.class);
  }

  @Test
  void should_reject_any_movement_on_an_inactive_account() {
    Cuenta inactiva = ahorrosCon("100").desactivar();

    assertThatThrownBy(() -> inactiva.aplicarMovimiento(MOVIMIENTO_ID, Monto.of("10"), FECHA))
        .isInstanceOf(CuentaInactivaException.class);
  }

  @Test
  void should_not_modify_the_original_account_when_a_movement_is_applied() {
    Cuenta original = ahorrosCon("100");

    original.aplicarMovimiento(MOVIMIENTO_ID, Monto.of("50"), FECHA);

    assertThat(original.getSaldoDisponible()).isEqualTo(Monto.of("100"));
  }

  @Test
  void should_recompute_balance_and_type_when_correcting_the_last_movement() {
    ResultadoMovimiento deposito =
        ahorrosCon("100").aplicarMovimiento(MOVIMIENTO_ID, Monto.of("600"), FECHA);

    ResultadoMovimiento corregido =
        deposito.cuenta().corregirUltimoMovimiento(deposito.movimiento(), Monto.of("-50"));

    assertThat(corregido.cuenta().getSaldoDisponible()).isEqualTo(Monto.of("50"));
    assertThat(corregido.movimiento())
        .isEqualTo(
            new Movimiento(
                MOVIMIENTO_ID,
                NUMERO,
                FECHA,
                TipoMovimiento.RETIRO,
                Monto.of("-50"),
                Monto.of("100"),
                Monto.of("50")));
  }

  @Test
  void should_fail_with_saldo_no_disponible_when_the_correction_overdraws() {
    ResultadoMovimiento deposito =
        ahorrosCon("100").aplicarMovimiento(MOVIMIENTO_ID, Monto.of("600"), FECHA);

    assertThatThrownBy(
            () ->
                deposito.cuenta().corregirUltimoMovimiento(deposito.movimiento(), Monto.of("-101")))
        .isInstanceOf(SaldoNoDisponibleException.class);
  }

  @Test
  void should_refuse_to_correct_a_movement_that_is_not_the_last_one() {
    Cuenta cuenta = ahorrosCon("100");
    ResultadoMovimiento primero = cuenta.aplicarMovimiento(MOVIMIENTO_ID, Monto.of("10"), FECHA);
    ResultadoMovimiento segundo =
        primero
            .cuenta()
            .aplicarMovimiento(new MovimientoId(UUID.randomUUID()), Monto.of("20"), FECHA);

    assertThatThrownBy(
            () -> segundo.cuenta().corregirUltimoMovimiento(primero.movimiento(), Monto.of("5")))
        .isInstanceOf(MovimientoNoModificableException.class);
  }

  @Test
  void should_refuse_to_correct_a_movement_of_another_account() {
    ResultadoMovimiento deposito =
        ahorrosCon("100").aplicarMovimiento(MOVIMIENTO_ID, Monto.of("10"), FECHA);
    Cuenta otra =
        Cuenta.abrir(new NumeroCuenta("225487"), TipoCuenta.CORRIENTE, Monto.of("110"), JOSE_LEMA);

    assertThatThrownBy(() -> otra.corregirUltimoMovimiento(deposito.movimiento(), Monto.of("5")))
        .isInstanceOf(MovimientoNoModificableException.class);
  }

  @Test
  void should_toggle_the_state_and_keep_the_same_instance_when_unchanged() {
    Cuenta activa = ahorrosCon("100");

    assertThat(activa.desactivar().isActiva()).isFalse();
    assertThat(activa.desactivar().activar().isActiva()).isTrue();
    assertThat(activa.activar()).isSameAs(activa);
  }

  @Test
  void should_change_the_account_type() {
    assertThat(ahorrosCon("100").cambiarTipo(TipoCuenta.CORRIENTE).getTipo())
        .isEqualTo(TipoCuenta.CORRIENTE);
  }

  @Test
  void should_reset_both_balances_when_redefining_the_initial_balance() {
    Cuenta cuenta = ahorrosCon("100").redefinirSaldoInicial(Monto.of("250"));

    assertThat(cuenta.getSaldoInicial()).isEqualTo(Monto.of("250"));
    assertThat(cuenta.getSaldoDisponible()).isEqualTo(Monto.of("250"));
  }

  @Test
  void should_restore_every_attribute_when_rebuilt_from_storage() {
    Cuenta cuenta =
        Cuenta.builder()
            .numeroCuenta(NUMERO)
            .tipo(TipoCuenta.CORRIENTE)
            .saldoInicial(Monto.of("100"))
            .saldoDisponible(Monto.of("700"))
            .activa(false)
            .clienteId(JOSE_LEMA)
            .version(VERSION)
            .build();

    assertThat(cuenta.getSaldoDisponible()).isEqualTo(Monto.of("700"));
    assertThat(cuenta.isActiva()).isFalse();
    assertThat(cuenta.getVersion()).isEqualTo(VERSION);
    assertThat(cuenta.desactivar().getVersion()).isEqualTo(VERSION);
  }

  @Test
  void should_reject_a_stored_negative_available_balance() {
    assertThatThrownBy(
            () ->
                Cuenta.builder()
                    .numeroCuenta(NUMERO)
                    .tipo(TipoCuenta.AHORROS)
                    .saldoInicial(Monto.of("0"))
                    .saldoDisponible(Monto.of("-1"))
                    .clienteId(JOSE_LEMA)
                    .build())
        .isInstanceOf(DatoInvalidoException.class);
  }

  @Test
  void should_be_equal_by_account_number() {
    Cuenta cuenta = ahorrosCon("100");

    assertThat(cuenta.desactivar()).isEqualTo(cuenta).hasSameHashCodeAs(cuenta);
    assertThat(cuenta.toString()).contains("478758");
  }
}
