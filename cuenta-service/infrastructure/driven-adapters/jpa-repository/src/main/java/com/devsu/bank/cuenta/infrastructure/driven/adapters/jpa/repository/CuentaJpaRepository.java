package com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.repository;

import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.entity.CuentaJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CuentaJpaRepository extends JpaRepository<CuentaJpaEntity, Long> {

  Optional<CuentaJpaEntity> findByNumeroCuenta(String numeroCuenta);

  /** SELECT … FOR UPDATE: the row stays locked until the surrounding transaction ends. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from CuentaJpaEntity c where c.numeroCuenta = :numeroCuenta")
  Optional<CuentaJpaEntity> findForUpdate(@Param("numeroCuenta") String numeroCuenta);

  boolean existsByNumeroCuenta(String numeroCuenta);

  List<CuentaJpaEntity> findByClienteIdOrderById(UUID clienteId);

  Page<CuentaJpaEntity> findByClienteId(UUID clienteId, Pageable pageable);
}
