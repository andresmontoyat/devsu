package com.devsu.bank.cuenta.application.support;

import com.devsu.bank.cuenta.application.port.outbound.ClienteProyeccionRepositoryPort;
import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.model.ClienteProyeccion;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryClienteProyeccionRepository implements ClienteProyeccionRepositoryPort {

  private final Map<ClienteId, ClienteProyeccion> proyecciones = new HashMap<>();

  @Override
  public Optional<ClienteProyeccion> findById(ClienteId clienteId) {
    return Optional.ofNullable(proyecciones.get(clienteId));
  }

  @Override
  public ClienteProyeccion save(ClienteProyeccion proyeccion) {
    proyecciones.put(proyeccion.clienteId(), proyeccion);
    return proyeccion;
  }
}
