package com.devsu.bank.cuenta.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

  private static final String POSTGRES_IMAGE = "postgres:17-alpine";
  private static final String KAFKA_IMAGE = "apache/kafka-native:4.3.2";
  private static final String VALKEY_IMAGE = "valkey/valkey:9.0-alpine";
  private static final int VALKEY_PORT = 6379;

  @Bean
  @ServiceConnection
  PostgreSQLContainer postgres() {
    return new PostgreSQLContainer(POSTGRES_IMAGE);
  }

  @Bean
  @ServiceConnection
  KafkaContainer kafka() {
    return new KafkaContainer(KAFKA_IMAGE);
  }

  @Bean
  @ServiceConnection(name = "redis")
  GenericContainer<?> valkey() {
    return new GenericContainer<>(VALKEY_IMAGE).withExposedPorts(VALKEY_PORT);
  }
}
