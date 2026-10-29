package com.devsu.bank.cuenta.application.port.outbound;

import java.util.UUID;

/** Consumer-side dedup (messaging.md §7.3), keyed by the {@code outbox-message-id} header. */
@FunctionalInterface
public interface ProcessedEventRepositoryPort {

  /** Atomically records the message; false when it had already been processed. */
  boolean registrarSiNuevo(UUID messageId);
}
