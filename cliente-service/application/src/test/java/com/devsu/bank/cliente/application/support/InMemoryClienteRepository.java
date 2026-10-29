package com.devsu.bank.cliente.application.support;

import com.devsu.bank.cliente.application.port.outbound.ClienteRepositoryPort;
import com.devsu.bank.cliente.application.query.Pagina;
import com.devsu.bank.cliente.application.query.PaginaSolicitud;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ClienteId;
import com.devsu.bank.cliente.domain.model.Identificacion;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Mimics the JPA adapter: version 0 on insert, +1 on every update. */
public final class InMemoryClienteRepository implements ClienteRepositoryPort {

  private final Map<ClienteId, Cliente> clientes = new LinkedHashMap<>();
  private final List<ClienteId> eliminados = new ArrayList<>();

  public Cliente guardarExistente(Cliente cliente) {
    clientes.put(cliente.getClienteId(), cliente);
    return cliente;
  }

  @Override
  public Cliente save(Cliente cliente) {
    Cliente previo = clientes.get(cliente.getClienteId());
    long version = previo == null ? 0 : previo.getVersion() + 1;
    Cliente persistido = conVersion(cliente, version);
    clientes.put(persistido.getClienteId(), persistido);
    return persistido;
  }

  @Override
  public Optional<Cliente> findById(ClienteId clienteId) {
    return Optional.ofNullable(clientes.get(clienteId));
  }

  @Override
  public boolean existsByIdentificacion(Identificacion identificacion) {
    return clientes.values().stream()
        .anyMatch(cliente -> cliente.getIdentificacion().equals(identificacion));
  }

  @Override
  public Pagina<Cliente> findAll(PaginaSolicitud solicitud) {
    List<Cliente> todos = List.copyOf(clientes.values());
    int desde = Math.min(solicitud.offset(), todos.size());
    int hasta = Math.min(desde + solicitud.tamano(), todos.size());
    return new Pagina<>(todos.subList(desde, hasta), solicitud, todos.size());
  }

  @Override
  public void delete(ClienteId clienteId) {
    clientes.remove(clienteId);
    eliminados.add(clienteId);
  }

  public List<ClienteId> eliminados() {
    return List.copyOf(eliminados);
  }

  public int cantidad() {
    return clientes.size();
  }

  private static Cliente conVersion(Cliente cliente, long version) {
    return Cliente.builder()
        .clienteId(cliente.getClienteId())
        .datos(cliente.getDatos())
        .contrasena(cliente.getContrasena())
        .activo(cliente.isActivo())
        .version(version)
        .build();
  }
}
