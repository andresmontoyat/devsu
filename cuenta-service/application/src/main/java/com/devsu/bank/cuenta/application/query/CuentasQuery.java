package com.devsu.bank.cuenta.application.query;

import com.devsu.bank.cuenta.domain.model.ClienteId;

/** List accounts, optionally only those of one client. */
public record CuentasQuery(ClienteId clienteId, PaginaSolicitud solicitud) {}
