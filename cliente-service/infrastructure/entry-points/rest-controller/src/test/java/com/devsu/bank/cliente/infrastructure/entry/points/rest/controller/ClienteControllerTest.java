package com.devsu.bank.cliente.infrastructure.entry.points.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devsu.bank.cliente.application.command.CreateClienteCommand;
import com.devsu.bank.cliente.application.command.PatchClienteCommand;
import com.devsu.bank.cliente.application.command.UpdateClienteCommand;
import com.devsu.bank.cliente.application.port.inbound.CreateClientePort;
import com.devsu.bank.cliente.application.port.inbound.DeleteClientePort;
import com.devsu.bank.cliente.application.port.inbound.GetClientePort;
import com.devsu.bank.cliente.application.port.inbound.ListClientesPort;
import com.devsu.bank.cliente.application.port.inbound.PatchClientePort;
import com.devsu.bank.cliente.application.port.inbound.UpdateClientePort;
import com.devsu.bank.cliente.application.query.Pagina;
import com.devsu.bank.cliente.application.query.PaginaSolicitud;
import com.devsu.bank.cliente.domain.exception.ClienteNoEncontradoException;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ClienteId;
import com.devsu.bank.cliente.domain.model.ContrasenaHash;
import com.devsu.bank.cliente.domain.model.DatosPersona;
import com.devsu.bank.cliente.domain.model.Genero;
import com.devsu.bank.cliente.domain.model.Identificacion;
import com.devsu.bank.cliente.domain.model.Telefono;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

  private static final ClienteId ID = ClienteId.of("0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e01");
  private static final String URL = "/clientes/" + ID.value();
  private static final MediaType MERGE_PATCH = MediaType.valueOf("application/merge-patch+json");
  private static final String CUERPO_CREAR =
      """
      {"nombre":"Jose Lema","genero":"MASCULINO","edad":35,"identificacion":"1712345678",
       "direccion":"Otavalo sn y principal","telefono":"098254785","contrasena":"1234",
       "estado":true}
      """;

  @Autowired private MockMvc mvc;
  @MockitoBean private CreateClientePort createCliente;
  @MockitoBean private GetClientePort getCliente;
  @MockitoBean private ListClientesPort listClientes;
  @MockitoBean private UpdateClientePort updateCliente;
  @MockitoBean private PatchClientePort patchCliente;
  @MockitoBean private DeleteClientePort deleteCliente;

  @Test
  void should_answer_201_with_location_and_no_password_when_creating() throws Exception {
    given(createCliente.execute(any())).willReturn(joseLema());

    mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(CUERPO_CREAR))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost" + URL))
        .andExpect(jsonPath("$.clienteId").value(ID.value().toString()))
        .andExpect(jsonPath("$.nombre").value("Jose Lema"))
        .andExpect(jsonPath("$.estado").value(true))
        .andExpect(jsonPath("$.contrasena").doesNotExist());
  }

  @Test
  void should_pass_every_field_to_the_create_port() throws Exception {
    given(createCliente.execute(any())).willReturn(joseLema());
    ArgumentCaptor<CreateClienteCommand> command =
        ArgumentCaptor.forClass(CreateClienteCommand.class);

    mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(CUERPO_CREAR));

    then(createCliente).should().execute(command.capture());
    org.assertj.core.api.Assertions.assertThat(command.getValue())
        .isEqualTo(
            new CreateClienteCommand(
                "Jose Lema",
                "MASCULINO",
                35,
                "1712345678",
                "Otavalo sn y principal",
                "098254785",
                "1234",
                true));
  }

  @Test
  void should_answer_400_with_field_errors_when_the_body_is_incomplete() throws Exception {
    mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDACION_FALLIDA"))
        .andExpect(jsonPath("$.errors[?(@.field == 'nombre')]").exists())
        .andExpect(jsonPath("$.errors[?(@.field == 'contrasena')]").exists());
  }

  @Test
  void should_answer_200_with_the_client_when_it_exists() throws Exception {
    given(getCliente.execute(ID)).willReturn(joseLema());

    mvc.perform(get(URL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.identificacion").value("1712345678"))
        .andExpect(jsonPath("$.genero").value("MASCULINO"));
  }

  @Test
  void should_answer_404_problem_details_when_the_client_does_not_exist() throws Exception {
    given(getCliente.execute(ID)).willThrow(new ClienteNoEncontradoException(ID));

    mvc.perform(get(URL))
        .andExpect(status().isNotFound())
        .andExpect(header().string("Content-Type", "application/problem+json"))
        .andExpect(jsonPath("$.code").value("CLIENTE_NO_ENCONTRADO"))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.instance").value(URL));
  }

  @Test
  void should_answer_400_when_the_id_is_not_a_uuid() throws Exception {
    mvc.perform(get("/clientes/no-es-uuid"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("DATO_INVALIDO"));
  }

  @Test
  void should_answer_a_page_with_metadata_when_listing() throws Exception {
    given(listClientes.execute(new PaginaSolicitud(0, 2)))
        .willReturn(new Pagina<>(List.of(joseLema()), new PaginaSolicitud(0, 2), 3));

    mvc.perform(get("/clientes").param("page", "0").param("size", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].nombre").value("Jose Lema"))
        .andExpect(jsonPath("$.page.number").value(0))
        .andExpect(jsonPath("$.page.size").value(2))
        .andExpect(jsonPath("$.page.totalElements").value(3))
        .andExpect(jsonPath("$.page.totalPages").value(2));
  }

  @Test
  void should_replace_the_client_with_put() throws Exception {
    given(updateCliente.execute(any())).willReturn(joseLema());
    ArgumentCaptor<UpdateClienteCommand> command =
        ArgumentCaptor.forClass(UpdateClienteCommand.class);

    mvc.perform(
            put(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nombre":"Jose Lema","genero":"MASCULINO","edad":35,
                     "identificacion":"1712345678","direccion":"Amazonas","telefono":"098254785",
                     "estado":false}
                    """))
        .andExpect(status().isOk());

    then(updateCliente).should().execute(command.capture());
    org.assertj.core.api.Assertions.assertThat(command.getValue().contrasena()).isNull();
    org.assertj.core.api.Assertions.assertThat(command.getValue().estado()).isFalse();
  }

  @Test
  void should_translate_a_merge_patch_into_a_partial_command() throws Exception {
    given(patchCliente.execute(any())).willReturn(joseLema());
    ArgumentCaptor<PatchClienteCommand> command =
        ArgumentCaptor.forClass(PatchClienteCommand.class);

    mvc.perform(patch(URL).contentType(MERGE_PATCH).content("{\"estado\":false,\"edad\":40}"))
        .andExpect(status().isOk());

    then(patchCliente).should().execute(command.capture());
    org.assertj.core.api.Assertions.assertThat(command.getValue())
        .isEqualTo(PatchClienteCommand.builder(ID).estado(false).edad(40).build());
  }

  @Test
  void should_reject_a_merge_patch_that_nulls_a_mandatory_field() throws Exception {
    mvc.perform(patch(URL).contentType(MERGE_PATCH).content("{\"nombre\":null}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("DATO_INVALIDO"))
        .andExpect(jsonPath("$.errors[0].field").value("nombre"));
  }

  @Test
  void should_reject_a_merge_patch_with_an_unknown_field() throws Exception {
    mvc.perform(patch(URL).contentType(MERGE_PATCH).content("{\"saldo\":10}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].field").value("saldo"));
  }

  @Test
  void should_reject_a_merge_patch_that_tries_to_change_the_identification() throws Exception {
    mvc.perform(patch(URL).contentType(MERGE_PATCH).content("{\"identificacion\":\"999999\"}"))
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.code").value("IDENTIFICACION_INMUTABLE"));
  }

  @Test
  void should_reject_a_merge_patch_with_a_wrong_type() throws Exception {
    mvc.perform(patch(URL).contentType(MERGE_PATCH).content("{\"edad\":\"cuarenta\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].field").value("edad"));
  }

  @Test
  void should_answer_204_when_deleting() throws Exception {
    mvc.perform(delete(URL)).andExpect(status().isNoContent());

    then(deleteCliente).should().execute(ID);
  }

  @Test
  void should_answer_404_when_deleting_an_unknown_client() throws Exception {
    willThrow(new ClienteNoEncontradoException(ID)).given(deleteCliente).execute(ID);

    mvc.perform(delete(URL)).andExpect(status().isNotFound());
  }

  private static Cliente joseLema() {
    DatosPersona datos =
        new DatosPersona(
            "Jose Lema",
            Genero.MASCULINO,
            35,
            new Identificacion("1712345678"),
            "Otavalo sn y principal",
            new Telefono("098254785"));
    return Cliente.registrar(ID, datos, new ContrasenaHash("$2a$10$hash"));
  }
}
