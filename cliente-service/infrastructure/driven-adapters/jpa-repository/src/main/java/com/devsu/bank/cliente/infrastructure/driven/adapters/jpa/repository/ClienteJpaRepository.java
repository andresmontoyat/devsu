package com.devsu.bank.cliente.infrastructure.driven.adapters.jpa.repository;

import com.devsu.bank.cliente.infrastructure.driven.adapters.jpa.entity.ClienteJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteJpaRepository extends JpaRepository<ClienteJpaEntity, Long> {

  Optional<ClienteJpaEntity> findByClienteId(UUID clienteId);

  boolean existsByIdentificacion(String identificacion);
}
