package com.devsu.bank.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.devsu.bank.cliente.support.AbstractIntegrationTest;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

/** F6: the whole service — HTTP, use case, PostgreSQL and the outbox relay — in one flow. */
class ClienteApiIT extends AbstractIntegrationTest {

  @Autowired private JdbcClient jdbc;

  @Test
  void should_create_read_and_publish_a_client_end_to_end() {
    RestClient client = RestClient.create(baseUrl());
    String identificacion = "IT" + System.nanoTime() % 1_000_000_000L;

    ResponseEntity<Map> creado =
        client
            .post()
            .uri("/clientes")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Map.of(
                    "nombre", "Marianela Montalvo",
                    "genero", "FEMENINO",
                    "edad", 30,
                    "identificacion", identificacion,
                    "direccion", "Amazonas y NNUU",
                    "telefono", "097548965",
                    "contrasena", "5678"))
            .retrieve()
            .toEntity(Map.class);
    String clienteId = (String) creado.getBody().get("clienteId");
    Map<String, Object> leido =
        client
            .get()
            .uri("/clientes/{id}", clienteId)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});

    assertThat(creado.getStatusCode().value()).isEqualTo(201);
    assertThat(leido).containsEntry("nombre", "Marianela Montalvo").containsEntry("estado", true);
    assertThat(hashGuardado(clienteId)).startsWith("$2a$10$").isNotEqualTo("5678");
    await()
        .atMost(Duration.ofSeconds(15))
        .untilAsserted(() -> assertThat(estadoOutbox(clienteId)).isEqualTo("PUBLISHED"));
  }

  @Test
  void should_publish_the_openapi_contract_and_the_swagger_ui() {
    RestClient client = RestClient.create(baseUrl());

    JsonNode docs = client.get().uri("/v3/api-docs").retrieve().body(JsonNode.class);
    ResponseEntity<String> swaggerUi =
        client.get().uri("/swagger-ui/index.html").retrieve().toEntity(String.class);

    assertThat(docs.path("openapi").asString()).isEqualTo("3.0.3");
    assertThat(docs.path("info").path("title").asString()).isEqualTo("cliente-service");
    assertThat(docs.path("paths").size()).isEqualTo(2);
    assertThat(docs.path("paths").has("/clientes/{clienteId}")).isTrue();
    assertThat(
            docs.at("/components/schemas/ClienteRequest/properties/contrasena/format").asString())
        .isEqualTo("password");
    assertThat(swaggerUi.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(swaggerUi.getBody()).contains("Swagger UI");
  }

  private String hashGuardado(String clienteId) {
    return jdbc.sql("SELECT contrasena_hash FROM cliente WHERE cliente_id = ?")
        .param(UUID.fromString(clienteId))
        .query(String.class)
        .single();
  }

  private String estadoOutbox(String clienteId) {
    return jdbc.sql("SELECT status FROM outbox_message WHERE message_key = ?")
        .param(clienteId)
        .query(String.class)
        .single();
  }
}
