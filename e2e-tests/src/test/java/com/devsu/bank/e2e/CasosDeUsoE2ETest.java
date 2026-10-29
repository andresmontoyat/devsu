package com.devsu.bank.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.karatelabs.core.Runner;
import io.karatelabs.core.SuiteResult;
import org.junit.jupiter.api.Test;

class CasosDeUsoE2ETest {

  @Test
  void should_complete_every_use_case_of_the_exercise_when_running_against_the_compose_stack() {
    SuiteResult result =
        Runner.path("classpath:e2e")
            .tags("@e2e")
            .systemProperty("e2e.clienteUrl", System.getProperty("e2e.clienteUrl"))
            .systemProperty("e2e.cuentaUrl", System.getProperty("e2e.cuentaUrl"))
            .outputDir("build/karate-reports")
            .parallel(1);

    assertThat(result.getErrors()).isEmpty();
    assertThat(result.getScenarioFailedCount()).isZero();
  }
}
