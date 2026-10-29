package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller;

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
}
