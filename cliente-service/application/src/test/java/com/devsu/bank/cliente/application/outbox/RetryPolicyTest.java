package com.devsu.bank.cliente.application.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RetryPolicyTest {

  private final RetryPolicy policy =
      new RetryPolicy(5, Duration.ofSeconds(30), Duration.ofSeconds(100));

  @ParameterizedTest
  @CsvSource({"1, 30", "2, 60", "3, 100", "10, 100"})
  void should_double_the_delay_per_attempt_up_to_the_cap(int intento, long segundos) {
    assertThat(policy.delayAfter(intento)).isEqualTo(Duration.ofSeconds(segundos));
  }

  @Test
  void should_be_exhausted_once_attempts_reach_the_maximum() {
    assertThat(policy.isExhausted(4)).isFalse();
    assertThat(policy.isExhausted(5)).isTrue();
  }

  @Test
  void should_reject_a_policy_without_attempts() {
    assertThatThrownBy(() -> new RetryPolicy(0, Duration.ofSeconds(1), Duration.ofSeconds(1)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_reject_a_claim_without_batch_size() {
    assertThatThrownBy(() -> new ClaimRequest("origin", 0, Duration.ofMinutes(1), "me"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
