package com.devsu.bank.cliente.infrastructure.driven.adapters.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cliente.application.outbox.OutboxMessage;
import com.devsu.bank.cliente.application.outbox.OutboxStatus;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

@Testcontainers
class KafkaMessagePublisherAdapterIT {

  private static final String TOPIC = "clientes.cliente.events";
  private static final String TRACEPARENT =
      "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01";

  @Container static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka-native:4.3.2");

  private static KafkaTemplate<String, String> template;

  @BeforeAll
  static void setUp() {
    Map<String, Object> config =
        Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            KAFKA.getBootstrapServers(),
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class,
            ProducerConfig.MAX_BLOCK_MS_CONFIG,
            3000);
    template = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(config));
  }

  @AfterAll
  static void tearDown() {
    template.destroy();
  }

  @Test
  void should_publish_the_payload_keyed_by_aggregate_with_dedup_and_trace_headers() {
    KafkaMessagePublisherAdapter adapter =
        new KafkaMessagePublisherAdapter(template, new PublisherSettings(Duration.ofSeconds(10)));
    OutboxMessage<String> mensaje = mensaje(TOPIC, TRACEPARENT);

    adapter.publish(mensaje);

    ConsumerRecord<String, String> record = leerUno(TOPIC);
    assertThat(record.key()).isEqualTo("cliente-1");
    assertThat(record.value()).isEqualTo("{\"evento\":1}");
    assertThat(header(record, KafkaMessagePublisherAdapter.OUTBOX_MESSAGE_ID))
        .isEqualTo(mensaje.id().toString());
    assertThat(header(record, KafkaMessagePublisherAdapter.TRACEPARENT)).isEqualTo(TRACEPARENT);
  }

  @Test
  void should_fail_when_the_broker_does_not_confirm_in_time() {
    KafkaTemplate<String, String> sinBroker =
        new KafkaTemplate<>(
            new DefaultKafkaProducerFactory<>(
                Map.of(
                    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    "localhost:1",
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    StringSerializer.class,
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                    StringSerializer.class,
                    ProducerConfig.MAX_BLOCK_MS_CONFIG,
                    500)));
    KafkaMessagePublisherAdapter adapter =
        new KafkaMessagePublisherAdapter(sinBroker, new PublisherSettings(Duration.ofSeconds(1)));

    assertThatThrownBy(() -> adapter.publish(mensaje(TOPIC, null)))
        .isInstanceOf(MessagePublishException.class);
    sinBroker.destroy();
  }

  private static OutboxMessage<String> mensaje(String topic, String traceparent) {
    return new OutboxMessage<>(
        UUID.randomUUID(),
        "cliente-service",
        topic,
        "cliente-1",
        "{\"evento\":1}",
        OutboxStatus.IN_FLIGHT,
        0,
        null,
        Instant.now(),
        null,
        null,
        traceparent);
  }

  private static ConsumerRecord<String, String> leerUno(String topic) {
    Map<String, Object> config =
        Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
            KAFKA.getBootstrapServers(),
            ConsumerConfig.GROUP_ID_CONFIG,
            "test-" + UUID.randomUUID(),
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
            "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class);
    try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(config)) {
      consumer.subscribe(List.of(topic));
      return consumer.poll(Duration.ofSeconds(15)).iterator().next();
    }
  }

  private static String header(ConsumerRecord<String, String> record, String name) {
    return new String(record.headers().lastHeader(name).value(), StandardCharsets.UTF_8);
  }
}
