package com.devsu.bank.cuenta.domain.reporte;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cuenta.domain.exception.RangoFechasInvalidoException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class RangoFechasTest {

  private static final ZoneId GUAYAQUIL = ZoneId.of("America/Guayaquil");

  @Test
  void should_parse_an_inclusive_iso_range() {
    RangoFechas rango = RangoFechas.parse("2022-02-01,2022-02-28");

    assertThat(rango.desde()).isEqualTo(LocalDate.of(2022, 2, 1));
    assertThat(rango.hasta()).isEqualTo(LocalDate.of(2022, 2, 28));
  }

  @Test
  void should_accept_a_single_day() {
    RangoFechas rango = RangoFechas.parse("2022-02-10");

    assertThat(rango.desde()).isEqualTo(rango.hasta());
  }

  @Test
  void should_cover_whole_days_in_the_given_zone() {
    RangoFechas rango = RangoFechas.parse("2022-02-10,2022-02-10");

    assertThat(rango.inicio(GUAYAQUIL)).isEqualTo(Instant.parse("2022-02-10T05:00:00Z"));
    assertThat(rango.finExclusivo(GUAYAQUIL)).isEqualTo(Instant.parse("2022-02-11T05:00:00Z"));
  }

  @Test
  void should_reject_a_range_that_ends_before_it_starts() {
    assertThatThrownBy(() -> RangoFechas.parse("2022-02-28,2022-02-01"))
        .isInstanceOf(RangoFechasInvalidoException.class);
  }

  @Test
  void should_reject_a_range_longer_than_the_maximum() {
    assertThatThrownBy(() -> RangoFechas.parse("2022-01-01,2023-01-03"))
        .isInstanceOf(RangoFechasInvalidoException.class)
        .hasMessageContaining(String.valueOf(RangoFechas.DIAS_MAX));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"10/2/2022", "2022-02-01,2022-02-10,2022-02-20", "2022-02-30"})
  void should_reject_malformed_ranges(String texto) {
    assertThatThrownBy(() -> RangoFechas.parse(texto))
        .isInstanceOf(RangoFechasInvalidoException.class);
  }
}
