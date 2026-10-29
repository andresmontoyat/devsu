package com.devsu.bank.cuenta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.devsu.bank.cuenta.support.AbstractIntegrationTest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.testcontainers.kafka.KafkaContainer;

/**
 * Asynchronous integration between the services: a ClienteCreado published by cliente-service is
 * all cuenta-service needs to open accounts for that client; ClienteEliminado deactivates them.
 */
class ClienteEventoIT extends AbstractIntegrationTest {

  private static final String TOPIC = "clientes.cliente.events";
  private static final Duration ESPERA = Duration.ofSeconds(20);
  private static final ParameterizedTypeReference<Map<String, Object>> JSON =
      new ParameterizedTypeReference<>() {};

  @Autowired private KafkaContainer kafka;

  @Test
  void should_open_accounts_for_a_client_known_only_through_its_event_and_close_them_on_delete() {
    String clienteId = UUID.randomUUID().toString();
    String numero = String.valueOf(200_000_000L + System.nanoTime() % 700_000_000L);
    RestClient client =
        RestClient.builder()
            .baseUrl(baseUrl())
            .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> {})
            .build();

    publicar(
        clienteId, evento("ClienteCreado", clienteId, ",\"nombre\":\"Nueva\",\"estado\":true", 0));
    await().atMost(ESPERA).until(() -> abrirCuenta(client, numero, clienteId) == 201);
    publicar(clienteId, evento("ClienteEliminado", clienteId, "", 1));

    await()
        .atMost(ESPERA)
        .untilAsserted(
            () ->
                assertThat(client.get().uri("/cuentas/{n}", numero).retrieve().body(JSON))
                    .containsEntry("estado", false));
  }

  private static int abrirCuenta(RestClient client, String numero, String clienteId) {
    return client
        .post()
        .uri("/cuentas")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            Map.of(
                "numeroCuenta",
                numero,
                "tipoCuenta",
                "Corriente",
                "saldoInicial",
                10,
                "clienteId",
                clienteId))
        .retrieve()
        .toBodilessEntity()
        .getStatusCode()
        .value();
  }

  private void publicar(String clave, String json) {
    Map<String, Object> config =
        Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    try (KafkaProducer<String, String> producer = new KafkaProducer<>(config)) {
      ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, clave, json);
      record
          .headers()
          .add("outbox-message-id", UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
      producer.send(record);
    }
  }

  private static String evento(String tipo, String clienteId, String extra, long version) {
    return "{\"header\":{\"origin\":\"cliente-service\"},\"body\":{\"aggregate\":\"%s\",\"eventName\":\"%s\",\"eventType\":\"EVENT\",\"payload\":{\"clienteId\":\"%s\"%s,\"version\":%d}}}"
        .formatted(clienteId, tipo, clienteId, extra, version);
  }
}
