package com.devsu.bank.cuenta.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Typed, validated contract of the service's own {@code cuenta:} block (application-yml.md R11).
 */
@Validated
@ConfigurationProperties(prefix = "cuenta")
public record CuentaProperties(
    @Valid @NotNull Reporte reporte, @Valid @NotNull Idempotency idempotency) {

  /** Calendar days of statements and filters are read in this zone, not in UTC. */
  public record Reporte(@NotNull ZoneId zona) {}

  public record Idempotency(
      @NotBlank String keyPrefix, @NotNull Duration ttl, @NotNull Duration inProgressTtl) {}
}
