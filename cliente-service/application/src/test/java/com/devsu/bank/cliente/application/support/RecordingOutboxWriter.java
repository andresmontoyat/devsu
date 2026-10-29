package com.devsu.bank.cliente.application.support;

import com.devsu.bank.cliente.application.outbox.EventMessage;
import com.devsu.bank.cliente.application.outbox.OutboxMessage;
import com.devsu.bank.cliente.application.port.outbound.OutboxWriter;
import java.util.ArrayList;
import java.util.List;

public final class RecordingOutboxWriter implements OutboxWriter {

  private final TransactionProbe probe;
  private final List<OutboxMessage<?>> mensajes = new ArrayList<>();
  private final List<Boolean> dentroDeTransaccion = new ArrayList<>();

  public RecordingOutboxWriter(TransactionProbe probe) {
    this.probe = probe;
  }

  @Override
  public <T> void save(OutboxMessage<T> message) {
    mensajes.add(message);
    dentroDeTransaccion.add(probe.isActive());
  }

  public List<OutboxMessage<?>> mensajes() {
    return List.copyOf(mensajes);
  }

  public boolean todosDentroDeTransaccion() {
    return !dentroDeTransaccion.isEmpty() && dentroDeTransaccion.stream().allMatch(b -> b);
  }

  public EventMessage<?> unicoEvento() {
    if (mensajes.size() != 1) {
      throw new AssertionError("Se esperaba 1 mensaje en el outbox y hay " + mensajes.size());
    }
    return (EventMessage<?>) mensajes.getFirst().payload();
  }
}
