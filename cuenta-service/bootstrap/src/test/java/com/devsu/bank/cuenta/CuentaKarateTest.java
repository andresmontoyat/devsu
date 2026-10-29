package com.devsu.bank.cuenta;

import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.bank.cuenta.support.AbstractIntegrationTest;
import io.karatelabs.core.Runner;
import io.karatelabs.core.SuiteResult;
import org.junit.jupiter.api.Test;

class CuentaKarateTest extends AbstractIntegrationTest {

  @Test
  void should_pass_every_karate_scenario_when_running_against_the_live_service() {
    SuiteResult result =
        Runner.path("classpath:karate")
            .tags("~@e2e")
            .systemProperty("karate.baseUrl", apiUrl())
            .outputDir("build/karate-reports")
            .parallel(1);

    assertThat(result.getErrors()).isEmpty();
    assertThat(result.getScenarioFailedCount()).isZero();
  }
}
