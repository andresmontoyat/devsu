package com.devsu.bank.cliente.application.support;

import com.devsu.bank.cliente.application.event.ClienteOutboxEvents;
import com.devsu.bank.cliente.application.outbox.OutboxEventFactory;
import java.util.Optional;

/** Wires the fakes every client use case test needs, sharing one transaction probe. */
public final class UseCaseHarness {

  public static final String ORIGIN = "cliente-service";

  public final TransactionProbe probe = new TransactionProbe();
  public final InMemoryClienteRepository repository = new InMemoryClienteRepository();
  public final RecordingOutboxWriter outbox = new RecordingOutboxWriter(probe);
  public final RecordingPasswordHasher hasher = new RecordingPasswordHasher(probe);
  public final ClienteOutboxEvents events =
      new ClienteOutboxEvents(
          outbox, new OutboxEventFactory(ORIGIN, ClienteTestData.CLOCK, Optional::empty));

  public <R> FakeTransaction<R> transaction() {
    return new FakeTransaction<>(probe);
  }
}
