package com.devsu.bank.cuenta.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cuenta.application.command.CorrectMovimientoCommand;
import com.devsu.bank.cuenta.application.command.RegisterMovimientoCommand;
import com.devsu.bank.cuenta.application.query.MovimientosQuery;
import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.application.query.PaginaSolicitud;
import com.devsu.bank.cuenta.application.support.CuentaHarness;
import com.devsu.bank.cuenta.domain.exception.MovimientoNoEncontradoException;
import com.devsu.bank.cuenta.domain.exception.MovimientoNoModificableException;
import com.devsu.bank.cuenta.domain.exception.SaldoNoDisponibleException;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.Movimiento;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.model.ResultadoMovimiento;
import com.devsu.bank.cuenta.domain.model.TipoMovimiento;
import com.devsu.bank.cuenta.domain.reporte.RangoFechas;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MovimientoUseCasesTest {

  private static final PaginaSolicitud PAGINA = new PaginaSolicitud(0, 10);

  private CuentaHarness harness;
  private RegisterMovimientoUseCase register;
  private CorrectMovimientoUseCase correct;

  @BeforeEach
  void setUp() {
    harness = new CuentaHarness().conCuentasDelEnunciado();
    register =
        new RegisterMovimientoUseCase(
            harness.transaction(), harness.cuentas, harness.movimientos, CuentaHarness.CLOCK);
    correct =
        new CorrectMovimientoUseCase(harness.transaction(), harness.cuentas, harness.movimientos);
  }

  @Test
  void should_correct_the_latest_movement_and_the_balance() {
    ResultadoMovimiento deposito = registrar("225487", "600");

    ResultadoMovimiento corregido =
        correct.execute(
            new CorrectMovimientoCommand(deposito.movimiento().id(), new BigDecimal("-50"), null));

    assertThat(corregido.movimiento().tipo()).isEqualTo(TipoMovimiento.RETIRO);
    assertThat(harness.cuentas.get("225487").getSaldoDisponible()).isEqualTo(Monto.of("50"));
    assertThat(harness.movimientos.findById(deposito.movimiento().id()))
        .contains(corregido.movimiento());
    assertThat(harness.cuentas.siempreBloqueoDentroDeTransaccion()).isTrue();
  }

  @Test
  void should_refuse_to_correct_an_older_movement() {
    ResultadoMovimiento primero = registrar("225487", "600");
    registrar("225487", "100");

    assertThatThrownBy(
            () ->
                correct.execute(
                    new CorrectMovimientoCommand(primero.movimiento().id(), BigDecimal.TEN, null)))
        .isInstanceOf(MovimientoNoModificableException.class);
  }

  @Test
  void should_answer_saldo_no_disponible_when_the_correction_overdraws() {
    ResultadoMovimiento deposito = registrar("225487", "600");

    assertThatThrownBy(
            () ->
                correct.execute(
                    new CorrectMovimientoCommand(
                        deposito.movimiento().id(), new BigDecimal("-101"), null)))
        .isInstanceOf(SaldoNoDisponibleException.class);
  }

  @Test
  void should_fail_with_not_found_when_correcting_an_unknown_movement() {
    assertThatThrownBy(
            () ->
                correct.execute(
                    new CorrectMovimientoCommand(MovimientoId.generar(), BigDecimal.TEN, null)))
        .isInstanceOf(MovimientoNoEncontradoException.class);
  }

  @Test
  void should_return_a_movement_by_id_or_fail_with_not_found() {
    ResultadoMovimiento deposito = registrar("225487", "600");
    GetMovimientoUseCase get = new GetMovimientoUseCase(harness.movimientos);

    assertThat(get.execute(deposito.movimiento().id())).isEqualTo(deposito.movimiento());
    assertThatThrownBy(() -> get.execute(MovimientoId.generar()))
        .isInstanceOf(MovimientoNoEncontradoException.class);
  }

  @Test
  void should_list_the_movements_of_an_account_within_a_local_date_range() {
    registrar("225487", "600");
    registrar("478758", "-575");
    ListMovimientosUseCase list =
        new ListMovimientosUseCase(harness.movimientos, CuentaHarness.ZONA);

    Pagina<Movimiento> mismoDia =
        list.execute(
            new MovimientosQuery(
                new NumeroCuenta("225487"), RangoFechas.parse("2022-02-10"), PAGINA));
    Pagina<Movimiento> otroDia =
        list.execute(new MovimientosQuery(null, RangoFechas.parse("2022-02-11"), PAGINA));
    Pagina<Movimiento> todos = list.execute(new MovimientosQuery(null, null, PAGINA));

    assertThat(mismoDia.contenido())
        .extracting(m -> m.numeroCuenta().value())
        .containsExactly("225487");
    assertThat(otroDia.contenido()).isEmpty();
    assertThat(todos.totalElementos()).isEqualTo(2);
  }

  private ResultadoMovimiento registrar(String numero, String valor) {
    return register.execute(
        new RegisterMovimientoCommand(numero, new BigDecimal(valor), null, null));
  }
}
