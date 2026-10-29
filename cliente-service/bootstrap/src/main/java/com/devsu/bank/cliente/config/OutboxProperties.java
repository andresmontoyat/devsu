package com.devsu.bank.cliente.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Typed, validated contract of the {@code outbox:} block (application-yml.md R11). */
@Validated
@ConfigurationProperties(prefix = "outbox")
public record OutboxProperties(
    boolean enabled,
    @Min(1) int batchSize,
    @Min(1) int maxAttempts,
    @NotNull Duration claimTtl,
    @Valid @NotNull Backoff backoff,
    @Valid @NotNull Publisher publisher,
    @Valid @NotNull Task task) {

  public record Backoff(@NotNull Duration baseDelay, @NotNull Duration maxDelay) {}

  public record Publisher(@NotNull Duration sendTimeout) {}

  public record Task(@NotNull Duration fixedDelay) {}
}
