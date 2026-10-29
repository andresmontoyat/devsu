package com.devsu.bank.cuenta.application.usecase;

import static com.devsu.bank.cuenta.application.support.CuentaHarness.AHORA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cuenta.application.command.RegisterMovimientoCommand;
import com.devsu.bank.cuenta.application.support.CuentaHarness;
import com.devsu.bank.cuenta.domain.exception.CuentaNoEncontradaException;
import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import com.devsu.bank.cuenta.domain.exception.SaldoNoDisponibleException;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.ResultadoMovimiento;
import com.devsu.bank.cuenta.domain.model.TipoMovimiento;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegisterMovimientoUseCaseTest {

  private CuentaHarness harness;
  private RegisterMovimientoUseCase useCase;

  @BeforeEach
  void setUp() {
    harness = new CuentaHarness().conCuentasDelEnunciado();
    useCase =
        new RegisterMovimientoUseCase(
            harness.transaction(), harness.cuentas, harness.movimientos, CuentaHarness.CLOCK);
  }

  @Test
  void should_apply_the_four_movements_of_the_exercise() {
    useCase.execute(comando("478758", "-575", null));
    useCase.execute(comando("225487", "600", null));
    useCase.execute(comando("495878", "150", null));
    useCase.execute(comando("496825", "-540", null));

    assertThat(harness.cuentas.get("478758").getSaldoDisponible()).isEqualTo(Monto.of("1425"));
    assertThat(harness.cuentas.get("225487").getSaldoDisponible()).isEqualTo(Monto.of("700"));
    assertThat(harness.cuentas.get("495878").getSaldoDisponible()).isEqualTo(Monto.of("150"));
    assertThat(harness.cuentas.get("496825").getSaldoDisponible()).isEqualTo(Monto.ZERO);
    assertThat(harness.movimientos.cantidad()).isEqualTo(4);
  }

  @Test
  void should_store_the_movement_dated_by_the_clock_with_both_balances() {
    ResultadoMovimiento resultado = useCase.execute(comando("225487", "600", null));

    assertThat(harness.movimientos.findById(resultado.movimiento().id()))
        .contains(resultado.movimiento());
    assertThat(resultado.movimiento().fecha()).isEqualTo(AHORA);
    assertThat(resultado.movimiento().tipo()).isEqualTo(TipoMovimiento.DEPOSITO);
    assertThat(resultado.movimiento().saldoAnterior()).isEqualTo(Monto.of("100"));
    assertThat(resultado.movimiento().saldo()).isEqualTo(Monto.of("700"));
  }

  @Test
  void should_lock_the_account_inside_the_transaction() {
    useCase.execute(comando("225487", "600", null));

    assertThat(harness.cuentas.siempreBloqueoDentroDeTransaccion()).isTrue();
  }

  @Test
  void should_answer_saldo_no_disponible_and_store_nothing_when_funds_are_short() {
    assertThatThrownBy(() -> useCase.execute(comando("495878", "-1", null)))
        .isInstanceOf(SaldoNoDisponibleException.class)
        .hasMessage("Saldo no disponible");
    assertThat(harness.movimientos.cantidad()).isZero();
    assertThat(harness.cuentas.get("495878").getSaldoDisponible()).isEqualTo(Monto.ZERO);
  }

  @Test
  void should_accept_a_declared_type_that_agrees_with_the_sign() {
    ResultadoMovimiento resultado = useCase.execute(comando("478758", "-575", "Retiro"));

    assertThat(resultado.movimiento().tipo()).isEqualTo(TipoMovimiento.RETIRO);
  }

  @Test
  void should_reject_a_declared_type_that_contradicts_the_sign() {
    assertThatThrownBy(() -> useCase.execute(comando("478758", "575", "RETIRO")))
        .isInstanceOf(DatoInvalidoException.class)
        .hasMessageContaining("tipoMovimiento");
  }

  @Test
  void should_fail_with_not_found_for_an_unknown_account() {
    assertThatThrownBy(() -> useCase.execute(comando("999999", "10", null)))
        .isInstanceOf(CuentaNoEncontradaException.class);
  }

  private static RegisterMovimientoCommand comando(String numero, String valor, String tipo) {
    return new RegisterMovimientoCommand(numero, new BigDecimal(valor), tipo, null);
  }
}
