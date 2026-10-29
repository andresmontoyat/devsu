package com.devsu.bank.cliente.infrastructure.entry.points.scheduler.task;

import com.devsu.bank.cliente.application.outbox.OutboxMessageWritten;
import com.devsu.bank.cliente.application.port.inbound.SendOutboxMessagesPort;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Drives the outbox dispatcher: right after each commit (low latency) and on a fixed poll (safety
 * net for anything a commit-time trigger missed). Requests coalesce into one queued drain, which
 * runs on its own executor — never inside the caller's transaction.
 */
public class OutboxDispatchScheduler {

  static final int MAX_RUNS_PER_DRAIN = 20;
  private static final Logger log = LoggerFactory.getLogger(OutboxDispatchScheduler.class);

  private final SendOutboxMessagesPort sendOutboxMessages;
  private final Executor executor;
  private final AtomicBoolean queued = new AtomicBoolean();

  public OutboxDispatchScheduler(SendOutboxMessagesPort sendOutboxMessages, Executor executor) {
    this.sendOutboxMessages = sendOutboxMessages;
    this.executor = executor;
  }

  @Scheduled(fixedDelayString = "${outbox.task.fixed-delay}")
  public void poll() {
    requestDispatch();
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onMessageWritten(OutboxMessageWritten event) {
    requestDispatch();
  }

  private void requestDispatch() {
    if (queued.compareAndSet(false, true)) {
      executor.execute(this::drain);
    }
  }

  private void drain() {
    queued.set(false);
    try {
      int runs = 1;
      while (sendOutboxMessages.execute() && runs < MAX_RUNS_PER_DRAIN) {
        runs++;
      }
    } catch (RuntimeException e) {
      log.warn("Fallo el despacho del outbox; se reintenta en el siguiente ciclo", e);
    }
  }
}
