package com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.repository;

import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.entity.ClienteProyeccionJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteProyeccionJpaRepository
    extends JpaRepository<ClienteProyeccionJpaEntity, UUID> {}
