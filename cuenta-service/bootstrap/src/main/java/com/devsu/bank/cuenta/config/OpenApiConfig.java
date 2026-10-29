package com.devsu.bank.cuenta.config;

import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import java.io.IOException;
import java.io.InputStream;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * Publishes the authored contract at /v3/api-docs instead of a spec rebuilt from the controllers,
 * so the documentation and the generated interfaces come from the same file.
 */
@Configuration(proxyBeanMethods = false)
class OpenApiConfig {

  @Bean
  OpenApiCustomizer publishedContract(
      @Value("classpath:openapi/cuenta-service.yaml") Resource contract) throws IOException {
    OpenAPI published = read(contract);
    return openApi ->
        openApi
            .openapi(published.getOpenapi())
            .info(published.getInfo())
            .servers(published.getServers())
            .tags(published.getTags())
            .paths(published.getPaths())
            .components(published.getComponents());
  }

  private static OpenAPI read(Resource contract) throws IOException {
    try (InputStream in = contract.getInputStream()) {
      return Yaml.mapper().readValue(in, OpenAPI.class);
    }
  }
}
