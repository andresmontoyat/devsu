package com.devsu.bank.cuenta.infrastructure.entry.points.kafka;

import com.devsu.bank.cuenta.domain.exception.DomainException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

/**
 * Transient failures (database busy, lock timeout) are retried with exponential backoff; poison
 * messages and business rejections go straight to {@code <topic>.dlt} — retrying them would only
 * block the partition (messaging.md §6.4).
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ClienteConsumerProperties.class)
public class KafkaConsumerErrorHandlingConfig {

  static final String DLT_SUFFIX = ".dlt";
  private static final int ANY_PARTITION = -1;

  @Bean
  DefaultErrorHandler kafkaErrorHandler(
      KafkaOperations<String, String> kafkaOperations, ClienteConsumerProperties properties) {
    DeadLetterPublishingRecoverer recoverer =
        new DeadLetterPublishingRecoverer(
            kafkaOperations,
            (record, ex) -> new TopicPartition(record.topic() + DLT_SUFFIX, ANY_PARTITION));
    DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff(properties.retry()));
    handler.addNotRetryableExceptions(InvalidClienteEventException.class, DomainException.class);
    return handler;
  }

  private static ExponentialBackOffWithMaxRetries backOff(ClienteConsumerProperties.Retry retry) {
    ExponentialBackOffWithMaxRetries backOff =
        new ExponentialBackOffWithMaxRetries(retry.maxRetries());
    backOff.setInitialInterval(retry.initialInterval().toMillis());
    backOff.setMultiplier(retry.multiplier());
    return backOff;
  }
}
