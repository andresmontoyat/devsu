package com.devsu.bank.cliente.application.outbox;

import java.time.Instant;

/** Canonical cross-service envelope (messaging.md §3). The row id travels as a broker header. */
public record EventMessage<T>(Header header, Body<T> body) {

  public static final String ENVELOPE_VERSION = "1.0.0";
  public static final String DEFAULT_SCHEMA_VERSION = "1.0.0";
  public static final String EVENT = "EVENT";

  public record Header(
      String traceId,
      String version,
      String schemaVersion,
      String origin,
      Instant timestamp,
      String locale) {}

  public record Body<T>(String aggregate, String eventName, String eventType, T payload) {}
}
