package com.devsu.bank.cuenta.domain.model;

import com.devsu.bank.cuenta.domain.exception.ClienteNoDisponibleException;

/**
 * Local, eventually consistent copy of a client of cliente-service, fed by its events. Versions
 * make it order-insensitive: stale or replayed events are ignored and a deletion is final.
 */
public record ClienteProyeccion(
    ClienteId clienteId, String nombre, boolean activo, boolean eliminado, long version) {

  public ClienteProyeccion {
    Requisitos.presente(clienteId, "clienteId");
    nombre = Requisitos.texto(nombre, "nombre");
  }

  public static ClienteProyeccion desde(
      ClienteId clienteId, String nombre, boolean activo, long version) {
    return new ClienteProyeccion(clienteId, nombre, activo, false, version);
  }

  public ClienteProyeccion actualizar(String nuevoNombre, boolean nuevoActivo, long nuevaVersion) {
    if (eliminado || nuevaVersion <= version) {
      return this;
    }
    return new ClienteProyeccion(clienteId, nuevoNombre, nuevoActivo, false, nuevaVersion);
  }

  public ClienteProyeccion eliminar(long nuevaVersion) {
    if (nuevaVersion <= version) {
      return this;
    }
    return new ClienteProyeccion(clienteId, nombre, false, true, nuevaVersion);
  }

  public void exigirDisponible() {
    if (!activo || eliminado) {
      throw new ClienteNoDisponibleException(clienteId);
    }
  }
}
