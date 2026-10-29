package com.devsu.bank.cuenta.application.usecase;

import static com.devsu.bank.cuenta.application.support.CuentaHarness.MARIANELA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cuenta.application.command.RegisterMovimientoCommand;
import com.devsu.bank.cuenta.application.query.EstadoCuentaQuery;
import com.devsu.bank.cuenta.application.support.CuentaHarness;
import com.devsu.bank.cuenta.domain.exception.ClienteNoEncontradoException;
import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.reporte.EstadoCuenta;
import com.devsu.bank.cuenta.domain.reporte.RangoFechas;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GenerateEstadoCuentaUseCaseTest {

  private static final RangoFechas FEBRERO = RangoFechas.parse("2022-02-01,2022-02-28");

  private CuentaHarness harness;
  private GenerateEstadoCuentaUseCase useCase;

  @BeforeEach
  void setUp() {
    harness = new CuentaHarness().conCuentasDelEnunciado();
    RegisterMovimientoUseCase register =
        new RegisterMovimientoUseCase(
            harness.transaction(), harness.cuentas, harness.movimientos, CuentaHarness.CLOCK);
    register.execute(new RegisterMovimientoCommand("225487", new BigDecimal("600"), null, null));
    register.execute(new RegisterMovimientoCommand("496825", new BigDecimal("-540"), null, null));
    register.execute(new RegisterMovimientoCommand("478758", new BigDecimal("-575"), null, null));
    useCase =
        new GenerateEstadoCuentaUseCase(
            harness.clientes, harness.cuentas, harness.movimientos, CuentaHarness.ZONA);
  }

  @Test
  void should_report_only_the_requested_client_with_balances_and_movements() {
    EstadoCuenta estado = useCase.execute(new EstadoCuentaQuery(MARIANELA, FEBRERO));

    assertThat(estado.cliente().nombre()).isEqualTo("Marianela Montalvo");
    assertThat(estado.cuentas())
        .extracting(c -> c.numeroCuenta().value() + "=" + c.saldoDisponible())
        .containsExactly("225487=700.00", "496825=0.00");
    assertThat(estado.movimientos())
        .extracting(m -> m.numeroCuenta().value() + ":" + m.movimiento())
        .containsExactlyInAnyOrder("225487:600.00", "496825:-540.00");
  }

  @Test
  void should_leave_out_movements_outside_the_range() {
    EstadoCuenta estado =
        useCase.execute(
            new EstadoCuentaQuery(MARIANELA, RangoFechas.parse("2022-03-01,2022-03-31")));

    assertThat(estado.movimientos()).isEmpty();
    assertThat(estado.cuentas()).allMatch(c -> c.totalCreditos().equals(Monto.ZERO));
  }

  @Test
  void should_fail_with_not_found_for_an_unknown_client() {
    ClienteId desconocido = ClienteId.of("11111111-1111-1111-1111-111111111111");

    assertThatThrownBy(() -> useCase.execute(new EstadoCuentaQuery(desconocido, FEBRERO)))
        .isInstanceOf(ClienteNoEncontradoException.class);
  }

  @Test
  void should_still_report_a_deleted_client_because_history_is_kept() {
    harness.clientes.save(harness.clientes.findById(MARIANELA).orElseThrow().eliminar(9));

    assertThat(useCase.execute(new EstadoCuentaQuery(MARIANELA, FEBRERO)).movimientos()).hasSize(2);
  }

  @Test
  void should_report_a_client_without_accounts_as_empty() {
    ClienteId sinCuentas = ClienteId.of("22222222-2222-2222-2222-222222222222");
    harness.clientes.save(
        com.devsu.bank.cuenta.domain.model.ClienteProyeccion.desde(sinCuentas, "Nuevo", true, 0));

    EstadoCuenta estado = useCase.execute(new EstadoCuentaQuery(sinCuentas, FEBRERO));

    assertThat(estado.cuentas()).isEmpty();
    assertThat(estado.movimientos()).isEmpty();
  }
}
