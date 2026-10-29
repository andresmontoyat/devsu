package com.devsu.bank.cliente.application.outbox.dispatch;

import com.devsu.bank.cliente.application.outbox.ClaimRequest;
import com.devsu.bank.cliente.application.outbox.OutboxMessage;
import com.devsu.bank.cliente.application.port.outbound.OutboxDispatchStore;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class FakeDispatchStore implements OutboxDispatchStore {

  private final List<OutboxMessage<String>> reclamables = new ArrayList<>();
  private final List<UUID> envioIniciado = new ArrayList<>();
  private final Map<UUID, OutboxMessage<String>> actualizados = new LinkedHashMap<>();
  private ClaimRequest ultimaSolicitud;

  void agregar(OutboxMessage<String> mensaje) {
    reclamables.add(mensaje);
  }

  @Override
  public List<OutboxMessage<String>> claim(ClaimRequest request) {
    ultimaSolicitud = request;
    return reclamables.stream().limit(request.batchSize()).toList();
  }

  @Override
  public void markSendStarted(UUID id) {
    envioIniciado.add(id);
  }

  @Override
  public void update(OutboxMessage<String> message) {
    actualizados.put(message.id(), message);
  }

  boolean envioIniciado(UUID id) {
    return envioIniciado.contains(id);
  }

  OutboxMessage<String> actualizado(UUID id) {
    return actualizados.get(id);
  }

  ClaimRequest ultimaSolicitud() {
    return ultimaSolicitud;
  }
}
