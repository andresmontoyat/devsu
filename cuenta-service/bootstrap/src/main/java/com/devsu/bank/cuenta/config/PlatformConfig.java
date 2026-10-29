package com.devsu.bank.cuenta.config;

import com.devsu.bank.cuenta.infrastructure.driven.adapters.redis.IdempotencySettings;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class PlatformConfig {

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  ZoneId zonaReporte(CuentaProperties properties) {
    return properties.reporte().zona();
  }

  @Bean
  IdempotencySettings idempotencySettings(CuentaProperties properties) {
    CuentaProperties.Idempotency idempotency = properties.idempotency();
    return new IdempotencySettings(
        idempotency.keyPrefix(), idempotency.ttl(), idempotency.inProgressTtl());
  }
}
