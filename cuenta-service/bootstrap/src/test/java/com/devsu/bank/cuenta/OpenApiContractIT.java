package com.devsu.bank.cuenta;

import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.bank.cuenta.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

class OpenApiContractIT extends AbstractIntegrationTest {

  @Test
  void should_publish_the_openapi_contract_and_the_swagger_ui() {
    RestClient client = RestClient.create(baseUrl());

    JsonNode docs = client.get().uri("/v3/api-docs").retrieve().body(JsonNode.class);
    ResponseEntity<String> swaggerUi =
        client.get().uri("/swagger-ui/index.html").retrieve().toEntity(String.class);

    assertThat(docs.path("openapi").asString()).isEqualTo("3.0.3");
    assertThat(docs.path("info").path("title").asString()).isEqualTo("cuenta-service");
    assertThat(docs.path("paths").size()).isEqualTo(5);
    assertThat(docs.path("paths").has("/reportes")).isTrue();
    assertThat(docs.at("/components/schemas/FilaEstadoCuenta/required").toString())
        .contains("Numero Cuenta", "Saldo Disponible");
    assertThat(swaggerUi.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(swaggerUi.getBody()).contains("Swagger UI");
  }
}
