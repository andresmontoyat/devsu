package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller;

import static com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.RestFixtures.MARIANELA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devsu.bank.cuenta.application.port.inbound.GenerateEstadoCuentaPort;
import com.devsu.bank.cuenta.application.query.EstadoCuentaQuery;
import com.devsu.bank.cuenta.domain.exception.ClienteNoEncontradoException;
import com.devsu.bank.cuenta.domain.model.ClienteProyeccion;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.model.ResultadoMovimiento;
import com.devsu.bank.cuenta.domain.model.TipoCuenta;
import com.devsu.bank.cuenta.domain.reporte.CuentaConMovimientos;
import com.devsu.bank.cuenta.domain.reporte.EstadoCuenta;
import com.devsu.bank.cuenta.domain.reporte.RangoFechas;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** F4: the response rows carry the exact keys of the exercise's sample JSON. */
@WebMvcTest(ReporteController.class)
class ReporteControllerTest {

  private static final RangoFechas FEBRERO = RangoFechas.parse("2022-02-01,2022-02-28");
  private static final String CLIENTE = MARIANELA.value().toString();

  @Autowired private MockMvc mvc;
  @MockitoBean private GenerateEstadoCuentaPort generateEstadoCuenta;

  @Test
  void should_return_the_statement_with_the_exercise_row_format() throws Exception {
    given(generateEstadoCuenta.execute(new EstadoCuentaQuery(MARIANELA, FEBRERO)))
        .willReturn(estadoDeMarianela());

    mvc.perform(
            get("/v1/reportes")
                .param("fechaInicio", "2022-02-01")
                .param("fechaFin", "2022-02-28")
                .param("cliente", CLIENTE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cliente.clienteId").value(CLIENTE))
        .andExpect(jsonPath("$.cliente.nombre").value("Marianela Montalvo"))
        .andExpect(jsonPath("$.fechaInicio").value("2022-02-01"))
        .andExpect(jsonPath("$.fechaFin").value("2022-02-28"))
        .andExpect(jsonPath("$.cuentas[0].numeroCuenta").value("225487"))
        .andExpect(jsonPath("$.cuentas[0].saldoDisponible").value(700.00))
        .andExpect(jsonPath("$.cuentas[0].totalCreditos").value(600.00))
        .andExpect(jsonPath("$.movimientos[0]['Fecha']").value("10/2/2022"))
        .andExpect(jsonPath("$.movimientos[0]['Cliente']").value("Marianela Montalvo"))
        .andExpect(jsonPath("$.movimientos[0]['Numero Cuenta']").value("225487"))
        .andExpect(jsonPath("$.movimientos[0]['Tipo']").value("Corriente"))
        .andExpect(jsonPath("$.movimientos[0]['Saldo Inicial']").value(100.00))
        .andExpect(jsonPath("$.movimientos[0]['Estado']").value(true))
        .andExpect(jsonPath("$.movimientos[0]['Movimiento']").value(600.00))
        .andExpect(jsonPath("$.movimientos[0]['Saldo Disponible']").value(700.00));
  }

  @Test
  void should_require_the_client_and_both_dates() throws Exception {
    mvc.perform(
            get("/v1/reportes").param("fechaInicio", "2022-02-01").param("fechaFin", "2022-02-28"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("PARAMETRO_INVALIDO"));
    mvc.perform(get("/v1/reportes").param("fechaInicio", "2022-02-01").param("cliente", CLIENTE))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("PARAMETRO_INVALIDO"));
    mvc.perform(
            get("/v1/reportes").param("fecha", "2022-02-01,2022-02-28").param("cliente", CLIENTE))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("PARAMETRO_INVALIDO"));
  }

  @Test
  void should_reject_an_inverted_date_range() throws Exception {
    mvc.perform(
            get("/v1/reportes")
                .param("fechaInicio", "2022-02-28")
                .param("fechaFin", "2022-02-01")
                .param("cliente", CLIENTE))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("RANGO_FECHAS_INVALIDO"));
  }

  @Test
  void should_reject_dates_that_are_not_iso_8601() throws Exception {
    mvc.perform(
            get("/v1/reportes")
                .param("fechaInicio", "10/2/2022")
                .param("fechaFin", "2022-02-28")
                .param("cliente", CLIENTE))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("PARAMETRO_INVALIDO"));
  }

  @Test
  void should_answer_404_for_an_unknown_client() throws Exception {
    given(generateEstadoCuenta.execute(any()))
        .willThrow(new ClienteNoEncontradoException(MARIANELA));

    mvc.perform(
            get("/v1/reportes")
                .param("fechaInicio", "2022-02-01")
                .param("fechaFin", "2022-02-01")
                .param("cliente", CLIENTE))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLIENTE_NO_ENCONTRADO"));
  }

  private static EstadoCuenta estadoDeMarianela() {
    ResultadoMovimiento deposito =
        com.devsu.bank.cuenta.domain.model.Cuenta.abrir(
                new NumeroCuenta("225487"), TipoCuenta.CORRIENTE, Monto.of("100"), MARIANELA)
            .aplicarMovimiento(
                MovimientoId.generar(), Monto.of("600"), Instant.parse("2022-02-10T15:00:00Z"));
    return EstadoCuenta.generar(
        ClienteProyeccion.desde(MARIANELA, "Marianela Montalvo", true, 0),
        FEBRERO,
        List.of(new CuentaConMovimientos(deposito.cuenta(), List.of(deposito.movimiento()))));
  }
}
