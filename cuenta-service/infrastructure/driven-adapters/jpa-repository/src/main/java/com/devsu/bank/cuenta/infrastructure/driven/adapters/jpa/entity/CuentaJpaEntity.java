package com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cuenta")
public class CuentaJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "numero_cuenta", nullable = false, unique = true, updatable = false, length = 20)
  private String numeroCuenta;

  @Column(name = "tipo_cuenta", nullable = false, length = 10)
  private String tipoCuenta;

  @Column(name = "saldo_inicial", nullable = false, precision = 15, scale = 2)
  private BigDecimal saldoInicial;

  @Column(name = "saldo_disponible", nullable = false, precision = 15, scale = 2)
  private BigDecimal saldoDisponible;

  @Column(nullable = false)
  private boolean estado;

  @Column(name = "cliente_id", nullable = false, updatable = false)
  private UUID clienteId;

  @Version private long version;

  public String getNumeroCuenta() {
    return numeroCuenta;
  }

  public void setNumeroCuenta(String numeroCuenta) {
    this.numeroCuenta = numeroCuenta;
  }

  public String getTipoCuenta() {
    return tipoCuenta;
  }

  public void setTipoCuenta(String tipoCuenta) {
    this.tipoCuenta = tipoCuenta;
  }

  public BigDecimal getSaldoInicial() {
    return saldoInicial;
  }

  public void setSaldoInicial(BigDecimal saldoInicial) {
    this.saldoInicial = saldoInicial;
  }

  public BigDecimal getSaldoDisponible() {
    return saldoDisponible;
  }

  public void setSaldoDisponible(BigDecimal saldoDisponible) {
    this.saldoDisponible = saldoDisponible;
  }

  public boolean isEstado() {
    return estado;
  }

  public void setEstado(boolean estado) {
    this.estado = estado;
  }

  public UUID getClienteId() {
    return clienteId;
  }

  public void setClienteId(UUID clienteId) {
    this.clienteId = clienteId;
  }

  public long getVersion() {
    return version;
  }
}
