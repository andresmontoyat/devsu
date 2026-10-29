package com.devsu.bank.cuenta.application.support;

import com.devsu.bank.cuenta.application.port.outbound.ProcessedEventRepositoryPort;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class InMemoryProcessedEvents implements ProcessedEventRepositoryPort {

  private final Set<UUID> procesados = new HashSet<>();

  @Override
  public boolean registrarSiNuevo(UUID messageId) {
    return procesados.add(messageId);
  }

  public boolean contiene(UUID messageId) {
    return procesados.contains(messageId);
  }
}
