package com.devsu.bank.cuenta.infrastructure.driven.adapters.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cuenta.application.idempotency.IdempotencyOutcome;
import com.devsu.bank.cuenta.application.idempotency.IdempotencyStoreNoDisponibleException;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class ValkeyIdempotencyStoreAdapterIT {

  private static final int PUERTO = 6379;
  private static final String HUELLA = "478758|-575|null";
  private static final IdempotencySettings SETTINGS =
      new IdempotencySettings("idem:movimiento:", Duration.ofHours(24), Duration.ofMinutes(1));

  @Container
  static final GenericContainer<?> VALKEY =
      new GenericContainer<>("valkey/valkey:9.0-alpine").withExposedPorts(PUERTO);

  private static LettuceConnectionFactory factory;
  private static StringRedisTemplate redis;

  private ValkeyIdempotencyStoreAdapter adapter;
  private String clave;

  @BeforeAll
  static void conectar() {
    factory = conexion(VALKEY.getHost(), VALKEY.getMappedPort(PUERTO));
    redis = new StringRedisTemplate(factory);
  }

  @AfterAll
  static void desconectar() {
    factory.destroy();
  }

  @BeforeEach
  void setUp() {
    adapter = new ValkeyIdempotencyStoreAdapter(redis, SETTINGS, CircuitBreaker.ofDefaults("t"));
    clave = UUID.randomUUID().toString();
  }

  @Test
  void should_reserve_a_new_key_and_report_it_in_progress_afterwards() {
    assertThat(adapter.reserve(clave, HUELLA)).isEqualTo(new IdempotencyOutcome.Reserved());
    assertThat(adapter.reserve(clave, HUELLA)).isEqualTo(new IdempotencyOutcome.InProgress());
    assertThat(redis.getExpire("idem:movimiento:" + clave)).isBetween(1L, 60L);
  }

  @Test
  void should_replay_the_movement_of_a_completed_key_for_a_day() {
    MovimientoId movimiento = MovimientoId.generar();
    adapter.reserve(clave, HUELLA);
    adapter.complete(clave, HUELLA, movimiento);

    assertThat(adapter.reserve(clave, HUELLA)).isEqualTo(new IdempotencyOutcome.Replay(movimiento));
    assertThat(redis.getExpire("idem:movimiento:" + clave))
        .isGreaterThan(Duration.ofHours(23).toSeconds());
  }

  @Test
  void should_detect_a_key_reused_with_another_body() {
    adapter.reserve(clave, HUELLA);

    assertThat(adapter.reserve(clave, "478758|-100|null"))
        .isEqualTo(new IdempotencyOutcome.FingerprintMismatch());
  }

  @Test
  void should_detect_a_completed_key_reused_with_another_body() {
    adapter.reserve(clave, HUELLA);
    adapter.complete(clave, HUELLA, MovimientoId.generar());

    assertThat(adapter.reserve(clave, "478758|-100|null"))
        .isEqualTo(new IdempotencyOutcome.FingerprintMismatch());
  }

  @Test
  void should_free_a_released_key() {
    adapter.reserve(clave, HUELLA);

    adapter.release(clave);

    assertThat(adapter.reserve(clave, HUELLA)).isEqualTo(new IdempotencyOutcome.Reserved());
  }

  @Test
  void should_report_the_store_unavailable_when_valkey_cannot_be_reached() {
    LettuceConnectionFactory caida = conexion("localhost", 1);
    ValkeyIdempotencyStoreAdapter sinValkey =
        new ValkeyIdempotencyStoreAdapter(
            new StringRedisTemplate(caida), SETTINGS, CircuitBreaker.ofDefaults("caida"));

    assertThatThrownBy(() -> sinValkey.reserve(clave, HUELLA))
        .isInstanceOf(IdempotencyStoreNoDisponibleException.class);
    caida.destroy();
  }

  @Test
  void should_fail_fast_without_calling_valkey_once_the_circuit_is_open() {
    CircuitBreaker abierto = CircuitBreaker.of("abierto", CircuitBreakerConfig.custom().build());
    abierto.transitionToOpenState();
    ValkeyIdempotencyStoreAdapter cortocircuitado =
        new ValkeyIdempotencyStoreAdapter(redis, SETTINGS, abierto);

    assertThatThrownBy(() -> cortocircuitado.reserve(clave, HUELLA))
        .isInstanceOf(IdempotencyStoreNoDisponibleException.class);
    assertThat(redis.hasKey("idem:movimiento:" + clave)).isFalse();
  }

  private static LettuceConnectionFactory conexion(String host, int puerto) {
    LettuceConnectionFactory nueva =
        new LettuceConnectionFactory(
            new RedisStandaloneConfiguration(host, puerto),
            LettuceClientConfiguration.builder().commandTimeout(Duration.ofMillis(500)).build());
    nueva.afterPropertiesSet();
    nueva.start();
    return nueva;
  }
}
