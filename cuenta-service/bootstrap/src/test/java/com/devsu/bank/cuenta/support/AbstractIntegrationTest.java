package com.devsu.bank.cuenta.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
public abstract class AbstractIntegrationTest {

  @LocalServerPort protected int port;

  protected String baseUrl() {
    return "http://localhost:" + port + "/api";
  }

  /** Base URL of version 1 of the REST API (api-design.md §5.1). */
  protected String apiUrl() {
    return baseUrl() + "/v1";
  }
}
