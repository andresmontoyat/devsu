package com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cuenta.application.command.CorrectMovimientoCommand;
import com.devsu.bank.cuenta.application.command.CreateCuentaCommand;
import com.devsu.bank.cuenta.application.command.PatchCuentaCommand;
import com.devsu.bank.cuenta.application.command.ReceivedClienteEvent;
import com.devsu.bank.cuenta.application.command.RegisterMovimientoCommand;
import com.devsu.bank.cuenta.application.command.UpdateCuentaCommand;
import com.devsu.bank.cuenta.application.query.CuentasQuery;
import com.devsu.bank.cuenta.application.query.EstadoCuentaQuery;
import com.devsu.bank.cuenta.application.query.MovimientosQuery;
import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.application.query.PaginaSolicitud;
import com.devsu.bank.cuenta.domain.exception.SaldoNoDisponibleException;
import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.model.ClienteProyeccion;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.model.ResultadoMovimiento;
import com.devsu.bank.cuenta.domain.model.TipoCuenta;
import com.devsu.bank.cuenta.domain.reporte.EstadoCuenta;
import com.devsu.bank.cuenta.domain.reporte.RangoFechas;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetricsAdaptersTest {

  private static final ClienteId CLIENTE = ClienteId.of("0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e02");
  private static final NumeroCuenta NUMERO = new NumeroCuenta("225487");
  private static final Cuenta CUENTA =
      Cuenta.abrir(NUMERO, TipoCuenta.CORRIENTE, Monto.of("100"), CLIENTE);
  private static final ResultadoMovimiento DEPOSITO =
      CUENTA.aplicarMovimiento(MovimientoId.generar(), Monto.of("600"), Instant.now());
  private static final PaginaSolicitud PAGINA = new PaginaSolicitud(0, 10);
  private static final RangoFechas RANGO = RangoFechas.parse("2022-02-01");

  private SimpleMeterRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new SimpleMeterRegistry();
  }

  @Test
  void should_count_saldo_no_disponible_as_a_failed_movement_and_rethrow_it() {
    RegisterMovimientoMetricsAdapter adapter = new RegisterMovimientoMetricsAdapter(registry);
    adapter.setDelegate(
        c -> {
          throw new SaldoNoDisponibleException();
        });

    assertThatThrownBy(
            () ->
                adapter.execute(
                    new RegisterMovimientoCommand("225487", new BigDecimal("-1"), null, null)))
        .isInstanceOf(SaldoNoDisponibleException.class);
    assertThat(
            registry
                .get("usecase.operation.errors")
                .tag("operation", "register_movimiento")
                .tag("error", "SaldoNoDisponibleException")
                .counter()
                .count())
        .isEqualTo(1.0);
  }

  @Test
  void should_record_every_account_context_operation() {
    CreateCuentaMetricsAdapter create = new CreateCuentaMetricsAdapter(registry);
    create.setDelegate(c -> CUENTA);
    GetCuentaMetricsAdapter get = new GetCuentaMetricsAdapter(registry);
    get.setDelegate(n -> CUENTA);
    ListCuentasMetricsAdapter list = new ListCuentasMetricsAdapter(registry);
    list.setDelegate(q -> new Pagina<>(List.of(), PAGINA, 0));
    UpdateCuentaMetricsAdapter update = new UpdateCuentaMetricsAdapter(registry);
    update.setDelegate(c -> CUENTA);
    PatchCuentaMetricsAdapter patch = new PatchCuentaMetricsAdapter(registry);
    patch.setDelegate(c -> CUENTA);
    RegisterMovimientoMetricsAdapter register = new RegisterMovimientoMetricsAdapter(registry);
    register.setDelegate(c -> DEPOSITO);
    CorrectMovimientoMetricsAdapter correct = new CorrectMovimientoMetricsAdapter(registry);
    correct.setDelegate(c -> DEPOSITO);
    GetMovimientoMetricsAdapter getMov = new GetMovimientoMetricsAdapter(registry);
    getMov.setDelegate(id -> DEPOSITO.movimiento());
    ListMovimientosMetricsAdapter listMov = new ListMovimientosMetricsAdapter(registry);
    listMov.setDelegate(q -> new Pagina<>(List.of(), PAGINA, 0));
    GenerateEstadoCuentaMetricsAdapter reporte = new GenerateEstadoCuentaMetricsAdapter(registry);
    reporte.setDelegate(
        q ->
            EstadoCuenta.generar(
                ClienteProyeccion.desde(CLIENTE, "Marianela", true, 0), RANGO, List.of()));
    ProjectClienteEventMetricsAdapter evento = new ProjectClienteEventMetricsAdapter(registry);
    evento.setDelegate(e -> {});

    create.execute(new CreateCuentaCommand("225487", "CORRIENTE", BigDecimal.TEN, true, "x"));
    get.execute(NUMERO);
    list.execute(new CuentasQuery(null, PAGINA));
    update.execute(new UpdateCuentaCommand("225487", "AHORROS", BigDecimal.TEN, true, "x"));
    patch.execute(new PatchCuentaCommand("225487", null, false));
    register.execute(new RegisterMovimientoCommand("225487", BigDecimal.TEN, null, null));
    correct.execute(new CorrectMovimientoCommand(MovimientoId.generar(), BigDecimal.TEN, null));
    getMov.execute(MovimientoId.generar());
    listMov.execute(new MovimientosQuery(null, null, PAGINA));
    reporte.execute(new EstadoCuentaQuery(CLIENTE, RANGO));
    evento.execute(new ReceivedClienteEvent(UUID.randomUUID(), "ClienteCreado", "x", "y", true, 0));

    assertThat(registry.get("usecase.operation.calls").tag("success", "true").counters())
        .hasSize(11);
  }
}
