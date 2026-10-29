package com.devsu.bank.cliente;

import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.bank.cliente.support.AbstractIntegrationTest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.testcontainers.kafka.KafkaContainer;

/** The ClienteCreado event reaches Kafka with the canonical envelope and the dedup header. */
class OutboxDispatchIT extends AbstractIntegrationTest {

  private static final String TOPIC = "clientes.cliente.events";

  @Autowired private KafkaContainer kafka;

  @Test
  void should_publish_ClienteCreado_keyed_by_client_with_envelope_and_headers() {
    String clienteId = crearCliente();

    ConsumerRecord<String, String> record = buscar(clienteId);

    assertThat(record.value())
        .contains("\"eventName\":\"ClienteCreado\"")
        .contains("\"origin\":\"cliente-service\"")
        .contains("\"nombre\":\"Juan Osorio\"")
        .doesNotContain("contrasena");
    assertThat(record.headers().lastHeader("outbox-message-id")).isNotNull();
    assertThat(
            new String(record.headers().lastHeader("content-type").value(), StandardCharsets.UTF_8))
        .isEqualTo("application/json");
  }

  private String crearCliente() {
    Map<?, ?> creado =
        RestClient.create(baseUrl())
            .post()
            .uri("/clientes")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Map.of(
                    "nombre", "Juan Osorio",
                    "genero", "MASCULINO",
                    "edad", 40,
                    "identificacion", "OD" + System.nanoTime() % 1_000_000_000L,
                    "direccion", "13 junio y Equinoccial",
                    "telefono", "098874587",
                    "contrasena", "1245"))
            .retrieve()
            .body(Map.class);
    return (String) creado.get("clienteId");
  }

  private ConsumerRecord<String, String> buscar(String clienteId) {
    try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerConfig())) {
      consumer.subscribe(List.of(TOPIC));
      long limite = System.nanoTime() + Duration.ofSeconds(20).toNanos();
      while (System.nanoTime() < limite) {
        for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
          if (clienteId.equals(record.key())) {
            return record;
          }
        }
      }
    }
    throw new AssertionError("No llego ClienteCreado para " + clienteId);
  }

  private Map<String, Object> consumerConfig() {
    return Map.of(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
        kafka.getBootstrapServers(),
        ConsumerConfig.GROUP_ID_CONFIG,
        "it-" + UUID.randomUUID(),
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
        "earliest",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class);
  }
}
