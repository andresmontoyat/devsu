package com.devsu.bank.cliente.application.outbox;

import java.util.UUID;

/** Signal that a row was stored; listeners dispatch it once the writing transaction commits. */
public record OutboxMessageWritten(UUID id) {}
