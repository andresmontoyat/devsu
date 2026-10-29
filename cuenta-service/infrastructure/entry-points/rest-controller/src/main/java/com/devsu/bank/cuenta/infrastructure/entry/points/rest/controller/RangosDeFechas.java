package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller;

import com.devsu.bank.cuenta.domain.exception.RangoFechasInvalidoException;
import com.devsu.bank.cuenta.domain.reporte.RangoFechas;
import java.time.LocalDate;

/** Builds the date range of the query parameters; RangoFechas enforces the rules themselves. */
final class RangosDeFechas {

  private RangosDeFechas() {}

  /** No dates means no date filter; a single date is rejected as an incomplete range. */
  static RangoFechas opcional(LocalDate fechaInicio, LocalDate fechaFin) {
    if (fechaInicio == null && fechaFin == null) {
      return null;
    }
    return new RangoFechas(fechaInicio, fechaFin);
  }

  /**
   * {@code fechaInicio}/{@code fechaFin}, or the deprecated {@code fecha=inicio,fin}; never both.
   */
  static RangoFechas requerido(String fechaDeprecada, LocalDate fechaInicio, LocalDate fechaFin) {
    if (fechaDeprecada == null) {
      return new RangoFechas(fechaInicio, fechaFin);
    }
    if (fechaInicio != null || fechaFin != null) {
      throw new RangoFechasInvalidoException("use fechaInicio y fechaFin, o fecha, pero no ambos");
    }
    return RangoFechas.parse(fechaDeprecada);
  }
}
