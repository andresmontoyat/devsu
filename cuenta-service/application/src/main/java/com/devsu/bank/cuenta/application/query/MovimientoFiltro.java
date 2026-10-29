package com.devsu.bank.cuenta.application.query;

import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import java.time.Instant;

/** Optional filters for listing movements; null means "no restriction". */
public record MovimientoFiltro(NumeroCuenta numeroCuenta, Instant desde, Instant hastaExclusivo) {}
