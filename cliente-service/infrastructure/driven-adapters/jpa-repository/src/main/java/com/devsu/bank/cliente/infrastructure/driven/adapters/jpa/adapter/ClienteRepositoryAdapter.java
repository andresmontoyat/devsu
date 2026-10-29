package com.devsu.bank.cliente.infrastructure.driven.adapters.jpa.adapter;

import com.devsu.bank.cliente.application.port.outbound.ClienteRepositoryPort;
import com.devsu.bank.cliente.application.query.Pagina;
import com.devsu.bank.cliente.application.query.PaginaSolicitud;
import com.devsu.bank.cliente.domain.exception.ClienteModificadoConcurrentementeException;
import com.devsu.bank.cliente.domain.exception.IdentificacionDuplicadaException;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ClienteId;
import com.devsu.bank.cliente.domain.model.Identificacion;
import com.devsu.bank.cliente.infrastructure.driven.adapters.jpa.entity.ClienteJpaEntity;
import com.devsu.bank.cliente.infrastructure.driven.adapters.jpa.mapper.ClienteEntityMapper;
import com.devsu.bank.cliente.infrastructure.driven.adapters.jpa.repository.ClienteJpaRepository;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class ClienteRepositoryAdapter implements ClienteRepositoryPort {

  private static final String UK_IDENTIFICACION = "uk_persona_identificacion";

  private final ClienteJpaRepository repository;

  public ClienteRepositoryAdapter(ClienteJpaRepository repository) {
    this.repository = repository;
  }

  /** Flushes so the caller gets the version that the database assigned (used by the events). */
  @Override
  public Cliente save(Cliente cliente) {
    ClienteJpaEntity entity =
        repository
            .findByClienteId(cliente.getClienteId().value())
            .map(existente -> actualizar(existente, cliente))
            .orElseGet(() -> ClienteEntityMapper.toNewEntity(cliente));
    try {
      return ClienteEntityMapper.toDomain(repository.saveAndFlush(entity));
    } catch (DataIntegrityViolationException e) {
      throw traducir(e, cliente);
    } catch (OptimisticLockingFailureException e) {
      throw new ClienteModificadoConcurrentementeException(cliente.getClienteId());
    }
  }

  @Override
  public Optional<Cliente> findById(ClienteId clienteId) {
    return repository.findByClienteId(clienteId.value()).map(ClienteEntityMapper::toDomain);
  }

  @Override
  public boolean existsByIdentificacion(Identificacion identificacion) {
    return repository.existsByIdentificacion(identificacion.value());
  }

  @Override
  public Pagina<Cliente> findAll(PaginaSolicitud solicitud) {
    PageRequest pageRequest = PageRequest.of(solicitud.pagina(), solicitud.tamano(), Sort.by("id"));
    Page<ClienteJpaEntity> page = repository.findAll(pageRequest);
    return new Pagina<>(
        page.map(ClienteEntityMapper::toDomain).getContent(), solicitud, page.getTotalElements());
  }

  @Override
  public void delete(ClienteId clienteId) {
    repository.findByClienteId(clienteId.value()).ifPresent(repository::delete);
  }

  /** The domain object carries the version it was read with: a mismatch means a lost update. */
  private static ClienteJpaEntity actualizar(ClienteJpaEntity existente, Cliente cliente) {
    if (existente.getVersion() != cliente.getVersion()) {
      throw new ClienteModificadoConcurrentementeException(cliente.getClienteId());
    }
    return ClienteEntityMapper.copyInto(cliente, existente);
  }

  private static RuntimeException traducir(DataIntegrityViolationException e, Cliente cliente) {
    String detalle = String.valueOf(e.getMostSpecificCause().getMessage());
    if (detalle.contains(UK_IDENTIFICACION)) {
      return new IdentificacionDuplicadaException(cliente.getIdentificacion());
    }
    return e;
  }
}
