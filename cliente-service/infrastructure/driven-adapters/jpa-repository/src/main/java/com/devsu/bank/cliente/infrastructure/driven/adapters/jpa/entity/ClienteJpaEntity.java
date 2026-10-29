package com.devsu.bank.cliente.infrastructure.driven.adapters.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "cliente")
@PrimaryKeyJoinColumn(name = "persona_id")
public class ClienteJpaEntity extends PersonaJpaEntity {

  @Column(name = "cliente_id", nullable = false, unique = true, updatable = false)
  private UUID clienteId;

  @Column(name = "contrasena_hash", nullable = false, length = 100)
  private String contrasenaHash;

  @Column(nullable = false)
  private boolean estado;

  public UUID getClienteId() {
    return clienteId;
  }

  public void setClienteId(UUID clienteId) {
    this.clienteId = clienteId;
  }

  public String getContrasenaHash() {
    return contrasenaHash;
  }

  public void setContrasenaHash(String contrasenaHash) {
    this.contrasenaHash = contrasenaHash;
  }

  public boolean isEstado() {
    return estado;
  }

  public void setEstado(boolean estado) {
    this.estado = estado;
  }
}
