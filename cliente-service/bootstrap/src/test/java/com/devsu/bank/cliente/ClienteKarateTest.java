package com.devsu.bank.cliente;

import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.bank.cliente.support.AbstractIntegrationTest;
import io.karatelabs.core.Runner;
import io.karatelabs.core.SuiteResult;
import org.junit.jupiter.api.Test;

class ClienteKarateTest extends AbstractIntegrationTest {

  @Test
  void should_pass_every_karate_scenario_when_running_against_the_live_service() {
    SuiteResult result =
        Runner.path("classpath:karate")
            .tags("~@e2e")
            .systemProperty("karate.baseUrl", baseUrl())
            .outputDir("build/karate-reports")
            .parallel(1);

    assertThat(result.getErrors()).isEmpty();
    assertThat(result.getScenarioFailedCount()).isZero();
  }
}
