package com.devsu.bank.cuenta.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cuenta.application.command.RegisterMovimientoCommand;
import com.devsu.bank.cuenta.application.idempotency.IdempotencyKeyEnCursoException;
import com.devsu.bank.cuenta.application.idempotency.IdempotencyKeyReutilizadaException;
import com.devsu.bank.cuenta.application.idempotency.IdempotencyStoreNoDisponibleException;
import com.devsu.bank.cuenta.application.support.CuentaHarness;
import com.devsu.bank.cuenta.domain.exception.SaldoNoDisponibleException;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.ResultadoMovimiento;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdempotentRegisterMovimientoUseCaseTest {

  private static final String CLAVE = "a3c1f0e2-retiro-575";

  private CuentaHarness harness;
  private IdempotentRegisterMovimientoUseCase useCase;

  @BeforeEach
  void setUp() {
    harness = new CuentaHarness().conCuentasDelEnunciado();
    RegisterMovimientoUseCase core =
        new RegisterMovimientoUseCase(
            harness.transaction(), harness.cuentas, harness.movimientos, CuentaHarness.CLOCK);
    useCase =
        new IdempotentRegisterMovimientoUseCase(
            core, harness.idempotencia, harness.movimientos, harness.cuentas);
  }

  @Test
  void should_apply_a_retried_request_only_once_and_answer_with_the_original_movement() {
    ResultadoMovimiento primero = useCase.execute(retiro(CLAVE, "-575"));
    ResultadoMovimiento reintento = useCase.execute(retiro(CLAVE, "-575"));

    assertThat(reintento.movimiento()).isEqualTo(primero.movimiento());
    assertThat(harness.movimientos.cantidad()).isEqualTo(1);
    assertThat(harness.cuentas.get("478758").getSaldoDisponible()).isEqualTo(Monto.of("1425"));
  }

  @Test
  void should_treat_equal_amounts_written_differently_as_the_same_request() {
    useCase.execute(retiro(CLAVE, "-575"));

    useCase.execute(retiro(CLAVE, "-575.00"));

    assertThat(harness.movimientos.cantidad()).isEqualTo(1);
  }

  @Test
  void should_reject_a_key_reused_with_a_different_body() {
    useCase.execute(retiro(CLAVE, "-575"));

    assertThatThrownBy(() -> useCase.execute(retiro(CLAVE, "-100")))
        .isInstanceOf(IdempotencyKeyReutilizadaException.class);
  }

  @Test
  void should_reject_a_concurrent_request_with_the_same_key() {
    harness.idempotencia.reserve(CLAVE, retiro(CLAVE, "-575").fingerprint());

    assertThatThrownBy(() -> useCase.execute(retiro(CLAVE, "-575")))
        .isInstanceOf(IdempotencyKeyEnCursoException.class);
    assertThat(harness.movimientos.cantidad()).isZero();
  }

  @Test
  void should_free_the_key_when_the_movement_fails_so_the_client_can_retry() {
    assertThatThrownBy(() -> useCase.execute(retiro(CLAVE, "-99999")))
        .isInstanceOf(SaldoNoDisponibleException.class);

    assertThat(harness.idempotencia.tiene(CLAVE)).isFalse();
  }

  @Test
  void should_run_without_the_store_when_no_key_is_sent() {
    harness.idempotencia.caer();

    useCase.execute(retiro(null, "-575"));

    assertThat(harness.movimientos.cantidad()).isEqualTo(1);
  }

  @Test
  void should_fail_closed_when_a_key_is_sent_but_the_store_is_down() {
    harness.idempotencia.caer();

    assertThatThrownBy(() -> useCase.execute(retiro(CLAVE, "-575")))
        .isInstanceOf(IdempotencyStoreNoDisponibleException.class);
    assertThat(harness.movimientos.cantidad()).isZero();
  }

  private static RegisterMovimientoCommand retiro(String clave, String valor) {
    return new RegisterMovimientoCommand("478758", new BigDecimal(valor), null, clave);
  }
}
