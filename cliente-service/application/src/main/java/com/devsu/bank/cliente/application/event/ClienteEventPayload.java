package com.devsu.bank.cliente.application.event;

import com.devsu.bank.cliente.domain.model.Cliente;

/**
 * Payload of ClienteCreado / ClienteActualizado. Minimal PII on purpose: consumers only need the
 * name and state; {@code version} lets them discard stale or replayed events.
 */
public record ClienteEventPayload(String clienteId, String nombre, boolean estado, long version) {

  public static ClienteEventPayload from(Cliente cliente) {
    return new ClienteEventPayload(
        cliente.getClienteId().value().toString(),
        cliente.getNombre(),
        cliente.isActivo(),
        cliente.getVersion());
  }
}
