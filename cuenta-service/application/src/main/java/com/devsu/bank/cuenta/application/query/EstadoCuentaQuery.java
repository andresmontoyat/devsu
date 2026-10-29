package com.devsu.bank.cuenta.application.query;

import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.reporte.RangoFechas;

public record EstadoCuentaQuery(ClienteId clienteId, RangoFechas rango) {}
