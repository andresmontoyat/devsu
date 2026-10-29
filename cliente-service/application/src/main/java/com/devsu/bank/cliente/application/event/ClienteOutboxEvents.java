package com.devsu.bank.cliente.application.event;

import com.devsu.bank.cliente.application.outbox.OutboxEvent;
import com.devsu.bank.cliente.application.outbox.OutboxEventFactory;
import com.devsu.bank.cliente.application.port.outbound.OutboxWriter;
import com.devsu.bank.cliente.domain.model.Cliente;

/**
 * Records the client lifecycle events in the outbox. Must be called inside the use case's
 * transaction so the event commits together with the state change.
 */
public final class ClienteOutboxEvents {

  public static final String TOPIC = "clientes.cliente.events";
  public static final String CLIENTE_CREADO = "ClienteCreado";
  public static final String CLIENTE_ACTUALIZADO = "ClienteActualizado";
  public static final String CLIENTE_ELIMINADO = "ClienteEliminado";

  private final OutboxWriter outboxWriter;
  private final OutboxEventFactory outboxEventFactory;

  public ClienteOutboxEvents(OutboxWriter outboxWriter, OutboxEventFactory outboxEventFactory) {
    this.outboxWriter = outboxWriter;
    this.outboxEventFactory = outboxEventFactory;
  }

  public void registrarCreado(Cliente cliente) {
    registrar(cliente, CLIENTE_CREADO, ClienteEventPayload.from(cliente));
  }

  public void registrarActualizado(Cliente cliente) {
    registrar(cliente, CLIENTE_ACTUALIZADO, ClienteEventPayload.from(cliente));
  }

  /** The tombstone is one version newer than the last state, so it wins over late updates. */
  public void registrarEliminado(Cliente cliente) {
    String id = cliente.getClienteId().value().toString();
    registrar(
        cliente, CLIENTE_ELIMINADO, new ClienteEliminadoPayload(id, cliente.getVersion() + 1));
  }

  private <T> void registrar(Cliente cliente, String type, T payload) {
    OutboxEvent<T> event =
        OutboxEvent.to(TOPIC)
            .key(cliente.getClienteId().value().toString())
            .type(type)
            .payload(payload);
    outboxWriter.save(outboxEventFactory.create(event));
  }
}
