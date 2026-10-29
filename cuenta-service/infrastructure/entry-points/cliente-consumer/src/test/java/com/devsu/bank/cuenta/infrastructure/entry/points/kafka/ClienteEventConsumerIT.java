package com.devsu.bank.cuenta.infrastructure.entry.points.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.kafka.KafkaContainer;

@SpringBootTest(classes = ConsumerTestApplication.class)
@Import(ClienteEventConsumerIT.KafkaConfig.class)
class ClienteEventConsumerIT {

  private static final String TOPIC = "clientes.cliente.events";
  private static final String DLT = TOPIC + ".dlt";
  private static final Duration ESPERA = Duration.ofSeconds(20);

  @TestConfiguration(proxyBeanMethods = false)
  static class KafkaConfig {
    @Bean
    @ServiceConnection
    KafkaContainer kafka() {
      return new KafkaContainer("apache/kafka-native:4.3.2");
    }
  }

  @Autowired private KafkaTemplate<String, String> kafkaTemplate;
  @Autowired private ConsumerTestApplication.RecordingProjector projector;
  @Autowired private KafkaContainer kafka;

  @BeforeEach
  void setUp() {
    projector.reiniciar(e -> {});
  }

  @Test
  void should_apply_a_valid_event_once() {
    String clienteId = UUID.randomUUID().toString();

    publicar(clienteId, UUID.randomUUID().toString(), evento(clienteId, "ClienteCreado"));

    await().atMost(ESPERA).until(() -> aplicadosDe(clienteId) == 1);
  }

  @Test
  void should_retry_a_transient_failure_and_then_apply_the_event() {
    String clienteId = UUID.randomUUID().toString();
    AtomicInteger intentos = new AtomicInteger();
    projector.reiniciar(
        e -> {
          if (e.clienteId().equals(clienteId) && intentos.incrementAndGet() < 3) {
            throw new IllegalStateException("base de datos ocupada");
          }
        });

    publicar(clienteId, UUID.randomUUID().toString(), evento(clienteId, "ClienteCreado"));

    await().atMost(ESPERA).until(() -> aplicadosDe(clienteId) == 1);
    assertThat(intentos.get()).isEqualTo(3);
  }

  @Test
  void should_send_a_malformed_message_to_the_dead_letter_topic_without_retrying() {
    String clave = UUID.randomUUID().toString();

    publicar(clave, UUID.randomUUID().toString(), "{esto no es json");

    assertThat(buscarEnDlt(clave)).isNotNull();
    assertThat(projector.llamadas.get()).isZero();
  }

  @Test
  void should_send_a_business_rejection_to_the_dead_letter_topic_after_a_single_attempt() {
    String clienteId = UUID.randomUUID().toString();
    projector.reiniciar(
        e -> {
          throw new DatoInvalidoException("nombre", "es obligatorio");
        });

    publicar(clienteId, UUID.randomUUID().toString(), evento(clienteId, "ClienteCreado"));

    ConsumerRecord<String, String> muerto = buscarEnDlt(clienteId);
    assertThat(muerto.headers().lastHeader("kafka_dlt-exception-fqcn")).isNotNull();
    assertThat(projector.llamadas.get()).isEqualTo(1);
  }

  @Test
  void should_send_a_message_without_dedup_header_to_the_dead_letter_topic() {
    String clienteId = UUID.randomUUID().toString();

    publicar(clienteId, null, evento(clienteId, "ClienteCreado"));

    assertThat(buscarEnDlt(clienteId)).isNotNull();
  }

  private long aplicadosDe(String clienteId) {
    return projector.aplicados.stream().filter(e -> e.clienteId().equals(clienteId)).count();
  }

  private void publicar(String clave, String messageId, String json) {
    ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, clave, json);
    if (messageId != null) {
      record.headers().add("outbox-message-id", messageId.getBytes(StandardCharsets.UTF_8));
    }
    kafkaTemplate.send(record).join();
  }

  private ConsumerRecord<String, String> buscarEnDlt(String clave) {
    Map<String, Object> config =
        Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
            kafka.getBootstrapServers(),
            ConsumerConfig.GROUP_ID_CONFIG,
            "dlt-" + UUID.randomUUID(),
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
            "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class);
    List<ConsumerRecord<String, String>> vistos = new ArrayList<>();
    try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(config)) {
      consumer.subscribe(List.of(DLT));
      await()
          .atMost(ESPERA)
          .until(
              () -> {
                consumer.poll(Duration.ofMillis(300)).forEach(vistos::add);
                return vistos.stream().anyMatch(r -> clave.equals(r.key()));
              });
    }
    return vistos.stream().filter(r -> clave.equals(r.key())).findFirst().orElseThrow();
  }

  private static String evento(String clienteId, String tipo) {
    return """
        {"header":{"origin":"cliente-service"},
         "body":{"aggregate":"%s","eventName":"%s","eventType":"EVENT",
                 "payload":{"clienteId":"%s","nombre":"Marianela","estado":true,"version":0}}}
        """
        .formatted(clienteId, tipo, clienteId);
  }
}
