package com.devsu.bank.cliente.application.port.outbound;

import com.devsu.bank.cliente.application.outbox.ClaimRequest;
import com.devsu.bank.cliente.application.outbox.OutboxMessage;
import java.util.List;
import java.util.UUID;

/** Dispatcher side of the outbox. Never injected into a business use case. */
public interface OutboxDispatchStore {

  /** Atomically claims due rows of this origin, respecting per-key order across instances. */
  List<OutboxMessage<String>> claim(ClaimRequest request);

  /** Committed before the broker call, so a reclaim knows the row may already have been sent. */
  void markSendStarted(UUID id);

  void update(OutboxMessage<String> message);
}
