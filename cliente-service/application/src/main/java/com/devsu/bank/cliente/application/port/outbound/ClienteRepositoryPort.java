package com.devsu.bank.cliente.application.port.outbound;

import com.devsu.bank.cliente.application.query.Pagina;
import com.devsu.bank.cliente.application.query.PaginaSolicitud;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ClienteId;
import com.devsu.bank.cliente.domain.model.Identificacion;
import java.util.Optional;

public interface ClienteRepositoryPort {

  /** Inserts or updates; the returned client carries the version assigned by persistence. */
  Cliente save(Cliente cliente);

  Optional<Cliente> findById(ClienteId clienteId);

  boolean existsByIdentificacion(Identificacion identificacion);

  Pagina<Cliente> findAll(PaginaSolicitud solicitud);

  void delete(ClienteId clienteId);
}
