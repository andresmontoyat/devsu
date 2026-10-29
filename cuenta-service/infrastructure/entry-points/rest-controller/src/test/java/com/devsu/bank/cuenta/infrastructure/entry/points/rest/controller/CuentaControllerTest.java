package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller;

import static com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.RestFixtures.MARIANELA;
import static com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.RestFixtures.corriente225487;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devsu.bank.cuenta.application.command.CreateCuentaCommand;
import com.devsu.bank.cuenta.application.command.PatchCuentaCommand;
import com.devsu.bank.cuenta.application.command.UpdateCuentaCommand;
import com.devsu.bank.cuenta.application.port.inbound.CreateCuentaPort;
import com.devsu.bank.cuenta.application.port.inbound.GetCuentaPort;
import com.devsu.bank.cuenta.application.port.inbound.ListCuentasPort;
import com.devsu.bank.cuenta.application.port.inbound.PatchCuentaPort;
import com.devsu.bank.cuenta.application.port.inbound.UpdateCuentaPort;
import com.devsu.bank.cuenta.application.query.CuentasQuery;
import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.application.query.PaginaSolicitud;
import com.devsu.bank.cuenta.domain.exception.ClienteNoDisponibleException;
import com.devsu.bank.cuenta.domain.exception.CuentaNoEncontradaException;
import com.devsu.bank.cuenta.domain.exception.NumeroCuentaDuplicadoException;
import com.devsu.bank.cuenta.domain.exception.SaldoInicialNoModificableException;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CuentaController.class)
class CuentaControllerTest {

  private static final NumeroCuenta NUMERO = new NumeroCuenta("225487");
  private static final String CLIENTE = MARIANELA.value().toString();
  private static final String CUERPO =
      """
      {"numeroCuenta":"225487","tipoCuenta":"Corriente","saldoInicial":100,"estado":true,
       "clienteId":"%s"}
      """
          .formatted(CLIENTE);

  @Autowired private MockMvc mvc;
  @MockitoBean private CreateCuentaPort createCuenta;
  @MockitoBean private GetCuentaPort getCuenta;
  @MockitoBean private ListCuentasPort listCuentas;
  @MockitoBean private UpdateCuentaPort updateCuenta;
  @MockitoBean private PatchCuentaPort patchCuenta;

  @Test
  void should_answer_201_with_location_when_opening_an_account() throws Exception {
    given(createCuenta.execute(any())).willReturn(corriente225487());
    ArgumentCaptor<CreateCuentaCommand> command =
        ArgumentCaptor.forClass(CreateCuentaCommand.class);

    mvc.perform(post("/cuentas").contentType(MediaType.APPLICATION_JSON).content(CUERPO))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/cuentas/225487"))
        .andExpect(jsonPath("$.numeroCuenta").value("225487"))
        .andExpect(jsonPath("$.tipoCuenta").value("CORRIENTE"))
        .andExpect(jsonPath("$.saldoDisponible").value(100.00))
        .andExpect(jsonPath("$.clienteId").value(CLIENTE));

    then(createCuenta).should().execute(command.capture());
    assertThat(command.getValue())
        .isEqualTo(
            new CreateCuentaCommand("225487", "Corriente", new BigDecimal("100"), true, CLIENTE));
  }

  @Test
  void should_answer_400_with_field_errors_when_the_body_is_incomplete() throws Exception {
    mvc.perform(post("/cuentas").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDACION_FALLIDA"))
        .andExpect(jsonPath("$.errors[?(@.field == 'numeroCuenta')]").exists())
        .andExpect(jsonPath("$.errors[?(@.field == 'saldoInicial')]").exists());
  }

  @Test
  void should_map_business_rejections_of_account_opening() throws Exception {
    given(createCuenta.execute(any()))
        .willThrow(new NumeroCuentaDuplicadoException(NUMERO))
        .willThrow(new ClienteNoDisponibleException(MARIANELA));

    mvc.perform(post("/cuentas").contentType(MediaType.APPLICATION_JSON).content(CUERPO))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("NUMERO_CUENTA_DUPLICADO"));
    mvc.perform(post("/cuentas").contentType(MediaType.APPLICATION_JSON).content(CUERPO))
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.code").value("CLIENTE_NO_DISPONIBLE"));
  }

  @Test
  void should_return_an_account_or_404() throws Exception {
    given(getCuenta.execute(NUMERO)).willReturn(corriente225487());
    given(getCuenta.execute(new NumeroCuenta("999999")))
        .willThrow(new CuentaNoEncontradaException(new NumeroCuenta("999999")));

    mvc.perform(get("/cuentas/225487"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.saldoInicial").value(100.00));
    mvc.perform(get("/cuentas/999999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CUENTA_NO_ENCONTRADA"));
  }

  @Test
  void should_list_the_accounts_of_a_client_with_page_metadata() throws Exception {
    PaginaSolicitud solicitud = new PaginaSolicitud(0, 20);
    given(listCuentas.execute(new CuentasQuery(MARIANELA, solicitud)))
        .willReturn(new Pagina<>(List.of(corriente225487()), solicitud, 1));

    mvc.perform(get("/cuentas").param("cliente", CLIENTE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].numeroCuenta").value("225487"))
        .andExpect(jsonPath("$.page.totalElements").value(1));
  }

  @Test
  void should_replace_an_account_with_put() throws Exception {
    given(updateCuenta.execute(any())).willReturn(corriente225487());

    mvc.perform(
            put("/cuentas/225487")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"tipoCuenta":"AHORROS","saldoInicial":100,"estado":false,"clienteId":"%s"}
                    """
                        .formatted(CLIENTE)))
        .andExpect(status().isOk());

    then(updateCuenta)
        .should()
        .execute(
            new UpdateCuentaCommand("225487", "AHORROS", new BigDecimal("100"), false, CLIENTE));
  }

  @Test
  void should_answer_409_when_the_initial_balance_can_no_longer_change() throws Exception {
    given(updateCuenta.execute(any())).willThrow(new SaldoInicialNoModificableException(NUMERO));

    mvc.perform(
            put("/cuentas/225487")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"tipoCuenta\":\"AHORROS\",\"saldoInicial\":5,\"estado\":true,\"clienteId\":\""
                        + CLIENTE
                        + "\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("SALDO_INICIAL_NO_MODIFICABLE"));
  }

  @Test
  void should_translate_a_merge_patch_and_reject_balance_members() throws Exception {
    given(patchCuenta.execute(any())).willReturn(corriente225487());
    MediaType mergePatch = MediaType.valueOf("application/merge-patch+json");

    mvc.perform(patch("/cuentas/225487").contentType(mergePatch).content("{\"estado\":false}"))
        .andExpect(status().isOk());
    mvc.perform(patch("/cuentas/225487").contentType(mergePatch).content("{\"saldoDisponible\":1}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].field").value("saldoDisponible"));
    mvc.perform(patch("/cuentas/225487").contentType(mergePatch).content("{\"estado\":null}"))
        .andExpect(status().isBadRequest());

    then(patchCuenta).should().execute(new PatchCuentaCommand("225487", null, false));
  }

  @Test
  void should_answer_405_because_accounts_are_never_deleted() throws Exception {
    mvc.perform(delete("/cuentas/225487"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.code").value("METODO_NO_PERMITIDO"));
  }
}
