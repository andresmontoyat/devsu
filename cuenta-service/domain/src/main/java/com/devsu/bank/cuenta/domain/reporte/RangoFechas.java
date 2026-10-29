package com.devsu.bank.cuenta.domain.reporte;

import com.devsu.bank.cuenta.domain.exception.RangoFechasInvalidoException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Inclusive range of calendar days for the account statement (F4), written {@code
 * yyyy-MM-dd,yyyy-MM-dd} or a single day. Capped so one request cannot scan years of movements.
 */
public record RangoFechas(LocalDate desde, LocalDate hasta) {

  public static final int DIAS_MAX = 366;
  private static final String SEPARADOR = ",";

  public RangoFechas {
    if (desde == null || hasta == null) {
      throw new RangoFechasInvalidoException("faltan las fechas");
    }
    if (hasta.isBefore(desde)) {
      throw new RangoFechasInvalidoException("la fecha final es anterior a la inicial");
    }
    if (ChronoUnit.DAYS.between(desde, hasta) + 1 > DIAS_MAX) {
      throw new RangoFechasInvalidoException("supera " + DIAS_MAX + " dias");
    }
  }

  public static RangoFechas parse(String texto) {
    if (texto == null || texto.isBlank()) {
      throw new RangoFechasInvalidoException("use yyyy-MM-dd,yyyy-MM-dd");
    }
    String[] partes = texto.split(SEPARADOR, -1);
    if (partes.length > 2) {
      throw new RangoFechasInvalidoException("use yyyy-MM-dd,yyyy-MM-dd");
    }
    LocalDate desde = fecha(partes[0]);
    return new RangoFechas(desde, partes.length == 2 ? fecha(partes[1]) : desde);
  }

  public Instant inicio(ZoneId zona) {
    return desde.atStartOfDay(zona).toInstant();
  }

  public Instant finExclusivo(ZoneId zona) {
    return hasta.plusDays(1).atStartOfDay(zona).toInstant();
  }

  private static LocalDate fecha(String texto) {
    try {
      return LocalDate.parse(texto.strip());
    } catch (DateTimeParseException e) {
      throw new RangoFechasInvalidoException("'" + texto.strip() + "' no es yyyy-MM-dd");
    }
  }
}
