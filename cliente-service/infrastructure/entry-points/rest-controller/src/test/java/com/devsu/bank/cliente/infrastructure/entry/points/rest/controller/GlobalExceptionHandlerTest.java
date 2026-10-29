package com.devsu.bank.cliente.infrastructure.entry.points.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devsu.bank.cliente.application.port.inbound.CreateClientePort;
import com.devsu.bank.cliente.application.port.inbound.DeleteClientePort;
import com.devsu.bank.cliente.application.port.inbound.GetClientePort;
import com.devsu.bank.cliente.application.port.inbound.ListClientesPort;
import com.devsu.bank.cliente.application.port.inbound.PatchClientePort;
import com.devsu.bank.cliente.application.port.inbound.UpdateClientePort;
import com.devsu.bank.cliente.domain.exception.ClienteMenorDeEdadException;
import com.devsu.bank.cliente.domain.exception.ClienteModificadoConcurrentementeException;
import com.devsu.bank.cliente.domain.exception.IdentificacionDuplicadaException;
import com.devsu.bank.cliente.domain.model.ClienteId;
import com.devsu.bank.cliente.domain.model.Identificacion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClienteController.class)
class GlobalExceptionHandlerTest {

  private static final ClienteId ID = ClienteId.of("0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e01");
  private static final String CUERPO =
      """
      {"nombre":"Jose","genero":"MASCULINO","edad":35,"identificacion":"1712345678",
       "direccion":"Otavalo","telefono":"098254785","contrasena":"1234"}
      """;

  @Autowired private MockMvc mvc;
  @MockitoBean private CreateClientePort createCliente;
  @MockitoBean private GetClientePort getCliente;
  @MockitoBean private ListClientesPort listClientes;
  @MockitoBean private UpdateClientePort updateCliente;
  @MockitoBean private PatchClientePort patchCliente;
  @MockitoBean private DeleteClientePort deleteCliente;

  @Test
  void should_map_a_duplicate_identification_to_409() throws Exception {
    given(createCliente.execute(any()))
        .willThrow(new IdentificacionDuplicadaException(new Identificacion("1712345678")));

    crear()
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("IDENTIFICACION_DUPLICADA"))
        .andExpect(
            jsonPath("$.type")
                .value("https://api.devsu-bank.example/errores/identificacion-duplicada"))
        .andExpect(jsonPath("$.title").value("Identificacion duplicada"));
  }

  @Test
  void should_map_an_under_age_client_to_422() throws Exception {
    given(createCliente.execute(any())).willThrow(new ClienteMenorDeEdadException(15, 18));

    crear()
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.code").value("CLIENTE_MENOR_DE_EDAD"));
  }

  @Test
  void should_map_a_concurrent_modification_to_409() throws Exception {
    given(createCliente.execute(any()))
        .willThrow(new ClienteModificadoConcurrentementeException(ID));

    crear()
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("CONFLICTO_DE_CONCURRENCIA"));
  }

  @Test
  void should_hide_internal_details_behind_a_500() throws Exception {
    given(createCliente.execute(any())).willThrow(new IllegalStateException("SQL secreto"));

    crear()
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code").value("ERROR_INTERNO"))
        .andExpect(jsonPath("$.detail").value("Ocurrio un error inesperado"));
  }

  @Test
  void should_map_an_unreadable_body_to_400() throws Exception {
    mvc.perform(post("/v1/clientes").contentType(MediaType.APPLICATION_JSON).content("{nope"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("CUERPO_INVALIDO"));
  }

  @Test
  void should_map_an_unsupported_media_type_to_415() throws Exception {
    mvc.perform(post("/v1/clientes").contentType(MediaType.TEXT_PLAIN).content("x"))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(jsonPath("$.code").value("TIPO_DE_CONTENIDO_NO_SOPORTADO"));
  }

  @Test
  void should_map_an_unknown_route_to_404() throws Exception {
    mvc.perform(get("/no-existe"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("RECURSO_NO_ENCONTRADO"));
  }

  @Test
  void should_map_a_wrong_query_parameter_type_to_400() throws Exception {
    mvc.perform(get("/v1/clientes").param("page", "uno"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("PARAMETRO_INVALIDO"));
  }

  private org.springframework.test.web.servlet.ResultActions crear() throws Exception {
    return mvc.perform(
        post("/v1/clientes").contentType(MediaType.APPLICATION_JSON).content(CUERPO));
  }
}
