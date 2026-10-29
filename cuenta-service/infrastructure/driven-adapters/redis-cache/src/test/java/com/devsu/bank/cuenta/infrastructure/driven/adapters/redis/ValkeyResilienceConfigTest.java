package com.devsu.bank.cuenta.infrastructure.driven.adapters.redis;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class ValkeyResilienceConfigTest {

  @Test
  void should_open_the_circuit_once_half_of_the_minimum_calls_fail() {
    CircuitBreaker breaker = new ValkeyResilienceConfig().idempotencyStoreCircuitBreaker();

    IntStream.range(0, 10)
        .forEach(
            i ->
                breaker.onError(
                    0, java.util.concurrent.TimeUnit.MILLISECONDS, new IllegalStateException()));

    assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
  }

  @Test
  void should_stay_closed_while_calls_succeed() {
    CircuitBreaker breaker = new ValkeyResilienceConfig().idempotencyStoreCircuitBreaker();

    IntStream.range(0, 10)
        .forEach(i -> breaker.onSuccess(0, java.util.concurrent.TimeUnit.MILLISECONDS));

    assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
  }
}
