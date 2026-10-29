package com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.repository;

import com.devsu.bank.cuenta.application.query.MovimientoFiltro;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.entity.MovimientoJpaEntity;
import org.springframework.data.jpa.domain.Specification;

/** Optional filters composed only when present, instead of "(:p is null or …)" JPQL. */
public final class MovimientoSpecifications {

  private MovimientoSpecifications() {}

  public static Specification<MovimientoJpaEntity> de(MovimientoFiltro filtro) {
    Specification<MovimientoJpaEntity> spec = Specification.unrestricted();
    if (filtro.numeroCuenta() != null) {
      String numero = filtro.numeroCuenta().value();
      spec = spec.and((root, query, cb) -> cb.equal(root.get("numeroCuenta"), numero));
    }
    if (filtro.desde() != null) {
      spec =
          spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fecha"), filtro.desde()));
    }
    if (filtro.hastaExclusivo() != null) {
      spec = spec.and((root, query, cb) -> cb.lessThan(root.get("fecha"), filtro.hastaExclusivo()));
    }
    return spec;
  }
}
