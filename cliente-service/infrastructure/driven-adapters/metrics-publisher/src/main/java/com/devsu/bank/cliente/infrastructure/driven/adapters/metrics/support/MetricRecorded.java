package com.devsu.bank.cliente.infrastructure.driven.adapters.metrics.support;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.function.Supplier;
import org.springframework.core.Ordered;

/**
 * Base of every metrics decorator (obs-metrics.md §4): timer {@code usecase.operation}, counter
 * {@code usecase.operation.calls} tagged by success, counter {@code usecase.operation.errors}
 * tagged by exception type. Order 10: first link of the chain.
 */
public abstract class MetricRecorded implements Ordered {

  public static final int ORDER = 10;
  private static final String TIMER = "usecase.operation";

  private final MeterRegistry registry;

  protected MetricRecorded(MeterRegistry registry) {
    this.registry = registry;
  }

  protected <T> T record(String entity, String operation, Supplier<T> execute) {
    Timer.Sample sample = Timer.start(registry);
    try {
      T result = execute.get();
      count(entity, operation, true);
      return result;
    } catch (RuntimeException e) {
      count(entity, operation, false);
      errors(entity, operation, e).increment();
      throw e;
    } finally {
      sample.stop(registry.timer(TIMER, "entity", entity, "operation", operation));
    }
  }

  protected void recordVoid(String entity, String operation, Runnable execute) {
    record(
        entity,
        operation,
        () -> {
          execute.run();
          return null;
        });
  }

  @Override
  public int getOrder() {
    return ORDER;
  }

  private void count(String entity, String operation, boolean success) {
    Counter.builder(TIMER + ".calls")
        .tags("entity", entity, "operation", operation, "success", String.valueOf(success))
        .register(registry)
        .increment();
  }

  private Counter errors(String entity, String operation, RuntimeException e) {
    return Counter.builder(TIMER + ".errors")
        .tags("entity", entity, "operation", operation, "error", e.getClass().getSimpleName())
        .register(registry);
  }
}
