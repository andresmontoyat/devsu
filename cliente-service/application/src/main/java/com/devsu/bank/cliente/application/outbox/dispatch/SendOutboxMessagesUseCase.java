package com.devsu.bank.cliente.application.outbox.dispatch;

import com.devsu.bank.cliente.application.outbox.OutboxDispatchSettings;
import com.devsu.bank.cliente.application.outbox.OutboxMessage;
import com.devsu.bank.cliente.application.port.inbound.SendOutboxMessagesPort;
import com.devsu.bank.cliente.application.port.outbound.MessagePublisherPort;
import com.devsu.bank.cliente.application.port.outbound.OutboxDispatchStore;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * One dispatcher run (messaging.md §5.3). Must run outside any caller transaction: the send mark
 * has to be committed before the broker is called.
 */
public final class SendOutboxMessagesUseCase implements SendOutboxMessagesPort {

  private final OutboxDispatchStore store;
  private final MessagePublisherPort publisher;
  private final OutboxDispatchSettings settings;

  public SendOutboxMessagesUseCase(
      OutboxDispatchStore store, MessagePublisherPort publisher, OutboxDispatchSettings settings) {
    this.store = store;
    this.publisher = publisher;
    this.settings = settings;
  }

  @Override
  public boolean execute() {
    List<OutboxMessage<String>> claimed = store.claim(settings.claimRequest());
    Set<String> blockedKeys = new HashSet<>();
    claimed.forEach(message -> dispatch(message, blockedKeys));
    return claimed.size() == settings.claimRequest().batchSize();
  }

  private void dispatch(OutboxMessage<String> message, Set<String> blockedKeys) {
    if (blockedKeys.contains(message.messageKey())) {
      store.update(message.released());
      return;
    }
    store.markSendStarted(message.id());
    try {
      publisher.publish(message);
      store.update(message.published(settings.clock().instant()));
    } catch (RuntimeException e) {
      blockedKeys.add(message.messageKey());
      store.update(
          message.attemptFailed(
              e.getMessage(), settings.retryPolicy(), settings.clock().instant()));
    }
  }
}
