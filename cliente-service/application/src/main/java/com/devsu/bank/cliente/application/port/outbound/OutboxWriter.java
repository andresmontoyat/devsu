package com.devsu.bank.cliente.application.port.outbound;

import com.devsu.bank.cliente.application.outbox.OutboxMessage;

/**
 * The only write port of the producer side: use cases store events here, inside their own
 * transaction. Name mirrors the codehunters SDK contract (ADR-0004).
 */
@FunctionalInterface
public interface OutboxWriter {

  <T> void save(OutboxMessage<T> message);
}
