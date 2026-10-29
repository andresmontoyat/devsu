package com.devsu.bank.cuenta.infrastructure.driven.adapters.redis;

import com.devsu.bank.cuenta.application.idempotency.IdempotencyOutcome;
import com.devsu.bank.cuenta.application.idempotency.IdempotencyStoreNoDisponibleException;
import com.devsu.bank.cuenta.application.port.outbound.IdempotencyStorePort;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.util.function.Supplier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Idempotency-Key store on Valkey. {@code SET NX PX} makes the reservation atomic across every
 * instance of the service. Stored values: {@code IN_PROGRESS|<fingerprint>} and {@code
 * DONE|<fingerprint>|<movimientoId>}.
 */
@Component
public class ValkeyIdempotencyStoreAdapter implements IdempotencyStorePort {

  private static final String IN_PROGRESS = "IN_PROGRESS";
  private static final String DONE = "DONE";
  private static final char SEP = '|';

  private final StringRedisTemplate redis;
  private final IdempotencySettings settings;
  private final CircuitBreaker circuitBreaker;

  public ValkeyIdempotencyStoreAdapter(
      StringRedisTemplate redis, IdempotencySettings settings, CircuitBreaker circuitBreaker) {
    this.redis = redis;
    this.settings = settings;
    this.circuitBreaker = circuitBreaker;
  }

  @Override
  public IdempotencyOutcome reserve(String key, String fingerprint) {
    return protegido(
        () -> {
          String redisKey = settings.keyPrefix() + key;
          String reserva = IN_PROGRESS + SEP + fingerprint;
          if (Boolean.TRUE.equals(
              redis.opsForValue().setIfAbsent(redisKey, reserva, settings.inProgressTtl()))) {
            return new IdempotencyOutcome.Reserved();
          }
          return interpretar(redis.opsForValue().get(redisKey), fingerprint);
        });
  }

  @Override
  public void complete(String key, String fingerprint, MovimientoId movimientoId) {
    String valor = DONE + SEP + fingerprint + SEP + movimientoId.value();
    protegido(
        () -> {
          redis.opsForValue().set(settings.keyPrefix() + key, valor, settings.ttl());
          return null;
        });
  }

  @Override
  public void release(String key) {
    protegido(() -> redis.delete(settings.keyPrefix() + key));
  }

  /** A key that expired between SET NX and GET is simply free again. */
  private static IdempotencyOutcome interpretar(String valor, String fingerprint) {
    if (valor == null) {
      return new IdempotencyOutcome.Reserved();
    }
    int primero = valor.indexOf(SEP);
    String estado = valor.substring(0, primero);
    String resto = valor.substring(primero + 1);
    if (IN_PROGRESS.equals(estado)) {
      return resto.equals(fingerprint)
          ? new IdempotencyOutcome.InProgress()
          : new IdempotencyOutcome.FingerprintMismatch();
    }
    int ultimo = resto.lastIndexOf(SEP);
    if (!resto.substring(0, ultimo).equals(fingerprint)) {
      return new IdempotencyOutcome.FingerprintMismatch();
    }
    return new IdempotencyOutcome.Replay(MovimientoId.of(resto.substring(ultimo + 1)));
  }

  private <T> T protegido(Supplier<T> operacion) {
    try {
      return circuitBreaker.executeSupplier(operacion);
    } catch (CallNotPermittedException | DataAccessException e) {
      throw new IdempotencyStoreNoDisponibleException(e);
    }
  }
}
