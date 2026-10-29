package com.devsu.bank.cuenta.infrastructure.driven.adapters.redis;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j core used programmatically (no Spring Boot starter): when Valkey keeps failing,
 * requests with an Idempotency-Key fail fast with 503 instead of each waiting for the timeout.
 */
@Configuration(proxyBeanMethods = false)
public class ValkeyResilienceConfig {

  private static final int VENTANA = 20;
  private static final int MINIMO_DE_LLAMADAS = 10;
  private static final float UMBRAL_DE_FALLOS = 50f;
  private static final Duration TIEMPO_ABIERTO = Duration.ofSeconds(10);

  @Bean
  CircuitBreaker idempotencyStoreCircuitBreaker() {
    CircuitBreakerConfig config =
        CircuitBreakerConfig.custom()
            .slidingWindowSize(VENTANA)
            .minimumNumberOfCalls(MINIMO_DE_LLAMADAS)
            .failureRateThreshold(UMBRAL_DE_FALLOS)
            .waitDurationInOpenState(TIEMPO_ABIERTO)
            .build();
    return CircuitBreaker.of("idempotency-store", config);
  }
}
