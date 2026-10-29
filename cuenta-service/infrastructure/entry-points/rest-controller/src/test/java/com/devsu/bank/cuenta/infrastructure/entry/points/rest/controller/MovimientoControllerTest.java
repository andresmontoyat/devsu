package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller;

import static com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.RestFixtures.MOVIMIENTO_ID;
import static com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.RestFixtures.deposito600;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devsu.bank.cuenta.application.command.CorrectMovimientoCommand;
import com.devsu.bank.cuenta.application.command.RegisterMovimientoCommand;
import com.devsu.bank.cuenta.application.idempotency.IdempotencyKeyEnCursoException;
import com.devsu.bank.cuenta.application.idempotency.IdempotencyKeyReutilizadaException;
import com.devsu.bank.cuenta.application.idempotency.IdempotencyStoreNoDisponibleException;
import com.devsu.bank.cuenta.application.port.inbound.CorrectMovimientoPort;
import com.devsu.bank.cuenta.application.port.inbound.GetMovimientoPort;
import com.devsu.bank.cuenta.application.port.inbound.ListMovimientosPort;
import com.devsu.bank.cuenta.application.port.inbound.RegisterMovimientoPort;
import com.devsu.bank.cuenta.application.query.MovimientosQuery;
import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.application.query.PaginaSolicitud;
import com.devsu.bank.cuenta.domain.exception.CuentaInactivaException;
import com.devsu.bank.cuenta.domain.exception.MovimientoNoModificableException;
import com.devsu.bank.cuenta.domain.exception.SaldoNoDisponibleException;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.reporte.RangoFechas;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(MovimientoController.class)
class MovimientoControllerTest {

  private static final String RETIRO = "{\"numeroCuenta\":\"478758\",\"valor\":-575}";
  private static final String URL = "/v1/movimientos/" + MOVIMIENTO_ID.value();

  @Autowired private MockMvc mvc;
  @MockitoBean private RegisterMovimientoPort registerMovimiento;
  @MockitoBean private CorrectMovimientoPort correctMovimiento;
  @MockitoBean private GetMovimientoPort getMovimiento;
  @MockitoBean private ListMovimientosPort listMovimientos;

  @Test
  void should_answer_201_with_both_balances_when_registering_a_movement() throws Exception {
    given(registerMovimiento.execute(any())).willReturn(deposito600());

    registrar("{\"numeroCuenta\":\"225487\",\"valor\":600,\"tipoMovimiento\":\"Deposito\"}", null)
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost" + URL))
        .andExpect(jsonPath("$.movimientoId").value(MOVIMIENTO_ID.value().toString()))
        .andExpect(jsonPath("$.fecha").value("2022-02-10T15:00:00Z"))
        .andExpect(jsonPath("$.tipoMovimiento").value("DEPOSITO"))
        .andExpect(jsonPath("$.valor").value(600.00))
        .andExpect(jsonPath("$.saldoInicial").value(100.00))
        .andExpect(jsonPath("$.saldoDisponible").value(700.00));

    then(registerMovimiento)
        .should()
        .execute(new RegisterMovimientoCommand("225487", new BigDecimal("600"), "Deposito", null));
  }

  @Test
  void should_pass_the_idempotency_key_header_to_the_use_case() throws Exception {
    given(registerMovimiento.execute(any())).willReturn(deposito600());

    registrar(RETIRO, "retiro-575-2022-02-10").andExpect(status().isCreated());

    then(registerMovimiento)
        .should()
        .execute(
            new RegisterMovimientoCommand(
                "478758", new BigDecimal("-575"), null, "retiro-575-2022-02-10"));
  }

  @Test
  void should_reject_a_malformed_idempotency_key() throws Exception {
    registrar(RETIRO, "x".repeat(256))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].field").value("Idempotency-Key"));
  }

  @Test
  void should_answer_422_saldo_no_disponible_when_funds_are_short() throws Exception {
    given(registerMovimiento.execute(any())).willThrow(new SaldoNoDisponibleException());

    registrar(RETIRO, null)
        .andExpect(status().isUnprocessableContent())
        .andExpect(header().string("Content-Type", "application/problem+json"))
        .andExpect(jsonPath("$.code").value("SALDO_NO_DISPONIBLE"))
        .andExpect(jsonPath("$.title").value("Saldo no disponible"))
        .andExpect(jsonPath("$.detail").value("Saldo no disponible"))
        .andExpect(jsonPath("$.instance").value("/v1/movimientos"));
  }

  @Test
  void should_map_an_inactive_account_to_422() throws Exception {
    given(registerMovimiento.execute(any()))
        .willThrow(new CuentaInactivaException(new NumeroCuenta("478758")));

    registrar(RETIRO, null)
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.code").value("CUENTA_INACTIVA"));
  }

  @Test
  void should_map_idempotency_conflicts_and_store_outages() throws Exception {
    given(registerMovimiento.execute(any()))
        .willThrow(new IdempotencyKeyEnCursoException())
        .willThrow(new IdempotencyKeyReutilizadaException())
        .willThrow(new IdempotencyStoreNoDisponibleException());

    registrar(RETIRO, "k1")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_EN_CURSO"));
    registrar(RETIRO, "k1")
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUTILIZADA"));
    registrar(RETIRO, "k1")
        .andExpect(status().isServiceUnavailable())
        .andExpect(header().string("Retry-After", "5"))
        .andExpect(jsonPath("$.code").value("IDEMPOTENCY_NO_DISPONIBLE"));
  }

  @Test
  void should_correct_a_movement_with_put_or_answer_409_when_it_is_not_the_latest()
      throws Exception {
    given(correctMovimiento.execute(any()))
        .willReturn(deposito600())
        .willThrow(new MovimientoNoModificableException(MOVIMIENTO_ID));

    mvc.perform(put(URL).contentType(MediaType.APPLICATION_JSON).content("{\"valor\":600}"))
        .andExpect(status().isOk());
    mvc.perform(put(URL).contentType(MediaType.APPLICATION_JSON).content("{\"valor\":600}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("MOVIMIENTO_NO_MODIFICABLE"));

    then(correctMovimiento)
        .should(org.mockito.Mockito.times(2))
        .execute(new CorrectMovimientoCommand(MOVIMIENTO_ID, new BigDecimal("600"), null));
  }

  @Test
  void should_get_a_movement_and_list_by_account_and_date_range() throws Exception {
    given(getMovimiento.execute(MOVIMIENTO_ID)).willReturn(deposito600().movimiento());
    PaginaSolicitud pagina = new PaginaSolicitud(0, 20);
    given(
            listMovimientos.execute(
                new MovimientosQuery(
                    new NumeroCuenta("225487"),
                    new RangoFechas(LocalDate.of(2022, 2, 1), LocalDate.of(2022, 2, 28)),
                    pagina)))
        .willReturn(new Pagina<>(List.of(deposito600().movimiento()), pagina, 1));

    mvc.perform(get(URL)).andExpect(status().isOk()).andExpect(jsonPath("$.valor").value(600.00));
    mvc.perform(
            get("/v1/movimientos")
                .param("cuenta", "225487")
                .param("fechaInicio", "2022-02-01")
                .param("fechaFin", "2022-02-28"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].numeroCuenta").value("225487"));
  }

  @Test
  void should_reject_an_inverted_date_range_and_deleting_movements() throws Exception {
    mvc.perform(
            get("/v1/movimientos")
                .param("fechaInicio", "2022-02-28")
                .param("fechaFin", "2022-02-01"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("RANGO_FECHAS_INVALIDO"));
    mvc.perform(delete(URL)).andExpect(status().isMethodNotAllowed());
  }

  @Test
  void should_reject_a_range_with_only_one_date() throws Exception {
    mvc.perform(get("/v1/movimientos").param("fechaInicio", "2022-02-01"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("RANGO_FECHAS_INVALIDO"));
  }

  @Test
  void should_reject_a_date_that_is_not_iso_8601() throws Exception {
    mvc.perform(
            get("/v1/movimientos")
                .param("fechaInicio", "01/02/2022")
                .param("fechaFin", "2022-02-28"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("PARAMETRO_INVALIDO"));
  }

  private ResultActions registrar(String cuerpo, String idempotencyKey) throws Exception {
    var request = post("/v1/movimientos").contentType(MediaType.APPLICATION_JSON).content(cuerpo);
    if (idempotencyKey != null) {
      request.header("Idempotency-Key", idempotencyKey);
    }
    return mvc.perform(request);
  }
}
