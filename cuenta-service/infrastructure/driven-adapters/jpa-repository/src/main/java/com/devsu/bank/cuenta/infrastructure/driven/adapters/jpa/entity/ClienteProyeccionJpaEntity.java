package com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/** {@code version} is cliente-service's version of the client, not a lock of this row. */
@Entity
@Table(name = "cliente_proyeccion")
public class ClienteProyeccionJpaEntity {

  @Id
  @Column(name = "cliente_id")
  private UUID clienteId;

  @Column(nullable = false, length = 100)
  private String nombre;

  @Column(nullable = false)
  private boolean activo;

  @Column(nullable = false)
  private boolean eliminado;

  @Column(nullable = false)
  private long version;

  public UUID getClienteId() {
    return clienteId;
  }

  public void setClienteId(UUID clienteId) {
    this.clienteId = clienteId;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public boolean isActivo() {
    return activo;
  }

  public void setActivo(boolean activo) {
    this.activo = activo;
  }

  public boolean isEliminado() {
    return eliminado;
  }

  public void setEliminado(boolean eliminado) {
    this.eliminado = eliminado;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
