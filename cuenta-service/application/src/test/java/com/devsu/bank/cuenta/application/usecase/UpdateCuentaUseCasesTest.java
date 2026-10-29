package com.devsu.bank.cuenta.application.usecase;

import static com.devsu.bank.cuenta.application.support.CuentaHarness.AHORA;
import static com.devsu.bank.cuenta.application.support.CuentaHarness.JOSE_LEMA;
import static com.devsu.bank.cuenta.application.support.CuentaHarness.MARIANELA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cuenta.application.command.PatchCuentaCommand;
import com.devsu.bank.cuenta.application.command.UpdateCuentaCommand;
import com.devsu.bank.cuenta.application.support.CuentaHarness;
import com.devsu.bank.cuenta.domain.exception.CuentaNoEncontradaException;
import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import com.devsu.bank.cuenta.domain.exception.SaldoInicialNoModificableException;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import com.devsu.bank.cuenta.domain.model.ResultadoMovimiento;
import com.devsu.bank.cuenta.domain.model.TipoCuenta;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateCuentaUseCasesTest {

  private static final String JOSE = JOSE_LEMA.value().toString();

  private CuentaHarness harness;
  private UpdateCuentaUseCase update;
  private PatchCuentaUseCase patch;

  @BeforeEach
  void setUp() {
    harness = new CuentaHarness().conCuentasDelEnunciado();
    update = new UpdateCuentaUseCase(harness.transaction(), harness.cuentas, harness.movimientos);
    patch = new PatchCuentaUseCase(harness.transaction(), harness.cuentas);
  }

  @Test
  void should_replace_type_state_and_initial_balance_while_there_are_no_movements() {
    Cuenta cuenta =
        update.execute(
            new UpdateCuentaCommand("478758", "CORRIENTE", new BigDecimal("2500"), false, JOSE));

    assertThat(cuenta.getTipo()).isEqualTo(TipoCuenta.CORRIENTE);
    assertThat(cuenta.getSaldoDisponible()).isEqualTo(Monto.of("2500"));
    assertThat(cuenta.isActiva()).isFalse();
    assertThat(cuenta.getVersion()).isEqualTo(1);
  }

  @Test
  void should_refuse_to_change_the_initial_balance_once_there_are_movements() {
    registrarDeposito("478758");

    assertThatThrownBy(
            () ->
                update.execute(
                    new UpdateCuentaCommand(
                        "478758", "AHORROS", new BigDecimal("5000"), true, JOSE)))
        .isInstanceOf(SaldoInicialNoModificableException.class);
  }

  @Test
  void should_keep_the_balances_when_the_same_initial_balance_is_sent_after_movements() {
    registrarDeposito("478758");

    Cuenta cuenta =
        update.execute(
            new UpdateCuentaCommand("478758", "CORRIENTE", new BigDecimal("2000"), true, JOSE));

    assertThat(cuenta.getSaldoDisponible()).isEqualTo(Monto.of("2010"));
    assertThat(cuenta.getTipo()).isEqualTo(TipoCuenta.CORRIENTE);
  }

  @Test
  void should_refuse_to_change_the_owner() {
    String otro = MARIANELA.value().toString();

    assertThatThrownBy(
            () ->
                update.execute(
                    new UpdateCuentaCommand(
                        "478758", "AHORROS", new BigDecimal("2000"), true, otro)))
        .isInstanceOf(DatoInvalidoException.class)
        .hasMessageContaining("clienteId");
  }

  @Test
  void should_fail_with_not_found_when_replacing_an_unknown_account() {
    assertThatThrownBy(
            () ->
                update.execute(
                    new UpdateCuentaCommand("999999", "AHORROS", BigDecimal.ONE, true, JOSE)))
        .isInstanceOf(CuentaNoEncontradaException.class);
  }

  @Test
  void should_patch_only_the_type() {
    Cuenta cuenta = patch.execute(new PatchCuentaCommand("478758", "corriente", null));

    assertThat(cuenta.getTipo()).isEqualTo(TipoCuenta.CORRIENTE);
    assertThat(cuenta.isActiva()).isTrue();
  }

  @Test
  void should_patch_only_the_state() {
    Cuenta cuenta = patch.execute(new PatchCuentaCommand("478758", null, false));

    assertThat(cuenta.isActiva()).isFalse();
    assertThat(cuenta.getTipo()).isEqualTo(TipoCuenta.AHORROS);
  }

  @Test
  void should_fail_with_not_found_when_patching_an_unknown_account() {
    assertThatThrownBy(() -> patch.execute(new PatchCuentaCommand("999999", null, false)))
        .isInstanceOf(CuentaNoEncontradaException.class);
  }

  private void registrarDeposito(String numero) {
    ResultadoMovimiento resultado =
        harness
            .cuentas
            .get(numero)
            .aplicarMovimiento(MovimientoId.generar(), Monto.of("10"), AHORA);
    harness.cuentas.update(resultado.cuenta());
    harness.movimientos.save(resultado.movimiento());
  }
}
