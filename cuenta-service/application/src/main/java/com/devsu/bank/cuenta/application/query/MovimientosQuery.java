package com.devsu.bank.cuenta.application.query;

import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.reporte.RangoFechas;

/** List movements, optionally of one account and within a date range. */
public record MovimientosQuery(
    NumeroCuenta numeroCuenta, RangoFechas rango, PaginaSolicitud solicitud) {}
