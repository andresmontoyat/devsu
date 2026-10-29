package com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.adapter;

import com.devsu.bank.cuenta.application.port.outbound.MovimientoRepositoryPort;
import com.devsu.bank.cuenta.application.query.MovimientoFiltro;
import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.application.query.PaginaSolicitud;
import com.devsu.bank.cuenta.domain.model.Movimiento;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.entity.MovimientoJpaEntity;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.mapper.CuentaEntityMapper;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.repository.MovimientoJpaRepository;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.repository.MovimientoSpecifications;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class MovimientoRepositoryAdapter implements MovimientoRepositoryPort {

  private final MovimientoJpaRepository repository;

  public MovimientoRepositoryAdapter(MovimientoJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public Movimiento save(Movimiento movimiento) {
    MovimientoJpaEntity entity =
        repository.findByMovimientoId(movimiento.id().value()).orElseGet(MovimientoJpaEntity::new);
    return CuentaEntityMapper.toDomain(
        repository.saveAndFlush(CuentaEntityMapper.copyInto(movimiento, entity)));
  }

  @Override
  public Optional<Movimiento> findById(MovimientoId id) {
    return repository.findByMovimientoId(id.value()).map(CuentaEntityMapper::toDomain);
  }

  @Override
  public Optional<Movimiento> findLatestByCuenta(NumeroCuenta numeroCuenta) {
    return repository
        .findFirstByNumeroCuentaOrderByIdDesc(numeroCuenta.value())
        .map(CuentaEntityMapper::toDomain);
  }

  @Override
  public boolean existsByCuenta(NumeroCuenta numeroCuenta) {
    return repository.existsByNumeroCuenta(numeroCuenta.value());
  }

  @Override
  public Pagina<Movimiento> findAll(MovimientoFiltro filtro, PaginaSolicitud solicitud) {
    PageRequest pageRequest = PageRequest.of(solicitud.pagina(), solicitud.tamano(), Sort.by("id"));
    Page<MovimientoJpaEntity> page =
        repository.findAll(MovimientoSpecifications.de(filtro), pageRequest);
    return new Pagina<>(
        page.map(CuentaEntityMapper::toDomain).getContent(), solicitud, page.getTotalElements());
  }

  @Override
  public List<Movimiento> findByCuentasBetween(
      List<NumeroCuenta> cuentas, Instant desde, Instant hastaExclusivo) {
    List<String> numeros = cuentas.stream().map(NumeroCuenta::value).toList();
    return repository
        .findByNumeroCuentaInAndFechaGreaterThanEqualAndFechaLessThanOrderByFechaAscIdAsc(
            numeros, desde, hastaExclusivo)
        .stream()
        .map(CuentaEntityMapper::toDomain)
        .toList();
  }
}
