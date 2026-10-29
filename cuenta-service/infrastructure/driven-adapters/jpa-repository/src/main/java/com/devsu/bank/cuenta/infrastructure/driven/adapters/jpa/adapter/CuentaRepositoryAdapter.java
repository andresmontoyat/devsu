package com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.adapter;

import com.devsu.bank.cuenta.application.port.outbound.CuentaRepositoryPort;
import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.application.query.PaginaSolicitud;
import com.devsu.bank.cuenta.domain.exception.CuentaModificadaConcurrentementeException;
import com.devsu.bank.cuenta.domain.exception.CuentaNoEncontradaException;
import com.devsu.bank.cuenta.domain.exception.NumeroCuentaDuplicadoException;
import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.entity.CuentaJpaEntity;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.mapper.CuentaEntityMapper;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.repository.CuentaJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class CuentaRepositoryAdapter implements CuentaRepositoryPort {

  private static final String UK_NUMERO = "uk_cuenta_numero";

  private final CuentaJpaRepository repository;

  public CuentaRepositoryAdapter(CuentaJpaRepository repository) {
    this.repository = repository;
  }

  /** Flushes so the unique constraint fires here, even when two creations race. */
  @Override
  public Cuenta insert(Cuenta cuenta) {
    try {
      CuentaJpaEntity entity = CuentaEntityMapper.copyInto(cuenta, new CuentaJpaEntity());
      return CuentaEntityMapper.toDomain(repository.saveAndFlush(entity));
    } catch (DataIntegrityViolationException e) {
      throw traducir(e, cuenta);
    }
  }

  @Override
  public Cuenta update(Cuenta cuenta) {
    CuentaJpaEntity entity =
        repository
            .findByNumeroCuenta(cuenta.getNumeroCuenta().value())
            .orElseThrow(() -> new CuentaNoEncontradaException(cuenta.getNumeroCuenta()));
    try {
      return CuentaEntityMapper.toDomain(repository.saveAndFlush(actualizar(entity, cuenta)));
    } catch (OptimisticLockingFailureException e) {
      throw new CuentaModificadaConcurrentementeException(cuenta.getNumeroCuenta());
    }
  }

  @Override
  public Optional<Cuenta> findByNumero(NumeroCuenta numeroCuenta) {
    return repository.findByNumeroCuenta(numeroCuenta.value()).map(CuentaEntityMapper::toDomain);
  }

  @Override
  public Optional<Cuenta> findByNumeroForUpdate(NumeroCuenta numeroCuenta) {
    return repository.findForUpdate(numeroCuenta.value()).map(CuentaEntityMapper::toDomain);
  }

  @Override
  public boolean existsByNumero(NumeroCuenta numeroCuenta) {
    return repository.existsByNumeroCuenta(numeroCuenta.value());
  }

  @Override
  public List<Cuenta> findByClienteId(ClienteId clienteId) {
    return repository.findByClienteIdOrderById(clienteId.value()).stream()
        .map(CuentaEntityMapper::toDomain)
        .toList();
  }

  @Override
  public Pagina<Cuenta> findAll(ClienteId clienteId, PaginaSolicitud solicitud) {
    PageRequest pageRequest = PageRequest.of(solicitud.pagina(), solicitud.tamano(), Sort.by("id"));
    Page<CuentaJpaEntity> page =
        clienteId == null
            ? repository.findAll(pageRequest)
            : repository.findByClienteId(clienteId.value(), pageRequest);
    return new Pagina<>(
        page.map(CuentaEntityMapper::toDomain).getContent(), solicitud, page.getTotalElements());
  }

  /** The domain object carries the version it was read with: a mismatch means a lost update. */
  private static CuentaJpaEntity actualizar(CuentaJpaEntity existente, Cuenta cuenta) {
    if (existente.getVersion() != cuenta.getVersion()) {
      throw new CuentaModificadaConcurrentementeException(cuenta.getNumeroCuenta());
    }
    return CuentaEntityMapper.copyInto(cuenta, existente);
  }

  private static RuntimeException traducir(DataIntegrityViolationException e, Cuenta cuenta) {
    String detalle = String.valueOf(e.getMostSpecificCause().getMessage());
    if (detalle.contains(UK_NUMERO)) {
      return new NumeroCuentaDuplicadoException(cuenta.getNumeroCuenta());
    }
    return e;
  }
}
