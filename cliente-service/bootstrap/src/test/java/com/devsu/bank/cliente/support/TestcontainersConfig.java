package com.devsu.bank.cliente.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

  private static final String POSTGRES_IMAGE = "postgres:17-alpine";
  private static final String KAFKA_IMAGE = "apache/kafka-native:4.3.2";

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
}
