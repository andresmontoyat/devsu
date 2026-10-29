package com.devsu.bank.cuenta.infrastructure.entry.points.kafka;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cuenta.consumer")
public record ClienteConsumerProperties(@NotBlank String topic, @Valid @NotNull Retry retry) {

  public record Retry(
      @NotNull Duration initialInterval,
      @DecimalMin("1.0") double multiplier,
      @Min(0) int maxRetries) {}
}
