package com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.repository;

import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.entity.MovimientoJpaEntity;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MovimientoJpaRepository
    extends JpaRepository<MovimientoJpaEntity, Long>,
        JpaSpecificationExecutor<MovimientoJpaEntity> {

  Optional<MovimientoJpaEntity> findByMovimientoId(UUID movimientoId);

  Optional<MovimientoJpaEntity> findFirstByNumeroCuentaOrderByIdDesc(String numeroCuenta);

  boolean existsByNumeroCuenta(String numeroCuenta);

  List<MovimientoJpaEntity>
      findByNumeroCuentaInAndFechaGreaterThanEqualAndFechaLessThanOrderByFechaAscIdAsc(
          Collection<String> numerosCuenta, Instant desde, Instant hastaExclusivo);
}
