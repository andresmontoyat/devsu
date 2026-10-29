package com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.adapter;

import com.devsu.bank.cuenta.application.port.outbound.ClienteProyeccionRepositoryPort;
import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.model.ClienteProyeccion;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.entity.ClienteProyeccionJpaEntity;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.mapper.CuentaEntityMapper;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.repository.ClienteProyeccionJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ClienteProyeccionRepositoryAdapter implements ClienteProyeccionRepositoryPort {

  private final ClienteProyeccionJpaRepository repository;

  public ClienteProyeccionRepositoryAdapter(ClienteProyeccionJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<ClienteProyeccion> findById(ClienteId clienteId) {
    return repository.findById(clienteId.value()).map(CuentaEntityMapper::toDomain);
  }

  @Override
  public ClienteProyeccion save(ClienteProyeccion proyeccion) {
    ClienteProyeccionJpaEntity entity =
        repository
            .findById(proyeccion.clienteId().value())
            .orElseGet(ClienteProyeccionJpaEntity::new);
    return CuentaEntityMapper.toDomain(
        repository.saveAndFlush(CuentaEntityMapper.copyInto(proyeccion, entity)));
  }
}
