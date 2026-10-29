package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller;

import java.time.ZoneId;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
class RestTestApplication {

  @Bean
  ZoneId zonaReporte() {
    return ZoneId.of("America/Guayaquil");
  }
}
