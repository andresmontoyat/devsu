package com.devsu.bank.cuenta.application.port.outbound;

import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.model.ClienteProyeccion;
import java.util.Optional;

public interface ClienteProyeccionRepositoryPort {

  Optional<ClienteProyeccion> findById(ClienteId clienteId);

  ClienteProyeccion save(ClienteProyeccion proyeccion);
}
