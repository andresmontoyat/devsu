package com.devsu.bank.cliente.application.port.outbound;

import com.devsu.bank.cliente.application.outbox.OutboxMessage;

/**
 * Broker-agnostic publish of one serialized outbox row; throws when the broker does not confirm.
 * Consumed only by the outbox dispatcher.
 */
@FunctionalInterface
public interface MessagePublisherPort {

  void publish(OutboxMessage<String> message);
}
