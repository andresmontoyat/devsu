package com.devsu.bank.cuenta.application.usecase;

import static com.devsu.bank.cuenta.application.support.CuentaHarness.JOSE_LEMA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cuenta.application.command.CreateCuentaCommand;
import com.devsu.bank.cuenta.application.support.CuentaHarness;
import com.devsu.bank.cuenta.domain.exception.ClienteNoDisponibleException;
import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import com.devsu.bank.cuenta.domain.exception.NumeroCuentaDuplicadoException;
import com.devsu.bank.cuenta.domain.model.ClienteProyeccion;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.TipoCuenta;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateCuentaUseCaseTest {

  private static final String JOSE = JOSE_LEMA.value().toString();

  private CuentaHarness harness;
  private CreateCuentaUseCase useCase;

  @BeforeEach
  void setUp() {
    harness = new CuentaHarness().conClientesDelEnunciado();
    useCase = new CreateCuentaUseCase(harness.transaction(), harness.cuentas, harness.clientes);
  }

  @Test
  void should_open_the_exercise_current_account_for_jose_lema() {
    Cuenta cuenta = useCase.execute(comando("585545", "1000", true, JOSE));

    assertThat(cuenta.getTipo()).isEqualTo(TipoCuenta.CORRIENTE);
    assertThat(cuenta.getSaldoDisponible()).isEqualTo(Monto.of("1000"));
    assertThat(cuenta.isActiva()).isTrue();
    assertThat(harness.cuentas.get("585545")).isEqualTo(cuenta);
  }

  @Test
  void should_open_an_inactive_account_when_requested() {
    assertThat(useCase.execute(comando("585545", "1000", false, JOSE)).isActiva()).isFalse();
  }

  @Test
  void should_reject_an_account_for_an_unknown_client() {
    assertThatThrownBy(
            () ->
                useCase.execute(
                    comando("585545", "10", true, "11111111-1111-1111-1111-111111111111")))
        .isInstanceOf(ClienteNoDisponibleException.class);
  }

  @Test
  void should_reject_an_account_for_an_inactive_client() {
    harness.clientes.save(ClienteProyeccion.desde(JOSE_LEMA, "Jose Lema", false, 1));

    assertThatThrownBy(() -> useCase.execute(comando("585545", "10", true, JOSE)))
        .isInstanceOf(ClienteNoDisponibleException.class);
  }

  @Test
  void should_reject_a_duplicated_account_number() {
    useCase.execute(comando("585545", "10", true, JOSE));

    assertThatThrownBy(() -> useCase.execute(comando("585545", "10", true, JOSE)))
        .isInstanceOf(NumeroCuentaDuplicadoException.class);
  }

  @Test
  void should_reject_a_negative_initial_balance_and_store_nothing() {
    assertThatThrownBy(() -> useCase.execute(comando("585545", "-5", true, JOSE)))
        .isInstanceOf(DatoInvalidoException.class);
    assertThat(harness.cuentas.get("585545")).isNull();
  }

  private static CreateCuentaCommand comando(
      String numero, String saldo, boolean estado, String cliente) {
    return new CreateCuentaCommand(numero, "Corriente", new BigDecimal(saldo), estado, cliente);
  }
}
