package com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Ledger row. The account is referenced by its business key, not by a {@code @ManyToOne}: another
 * aggregate is referenced by identity (DDD), and no lazy proxy can leak through the port.
 */
@Entity
@Table(name = "movimiento")
public class MovimientoJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "movimiento_id", nullable = false, unique = true, updatable = false)
  private UUID movimientoId;

  @Column(name = "numero_cuenta", nullable = false, updatable = false, length = 20)
  private String numeroCuenta;

  @Column(nullable = false, updatable = false)
  private Instant fecha;

  @Column(name = "tipo_movimiento", nullable = false, length = 10)
  private String tipoMovimiento;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal valor;

  @Column(name = "saldo_anterior", nullable = false, precision = 15, scale = 2)
  private BigDecimal saldoAnterior;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal saldo;

  public UUID getMovimientoId() {
    return movimientoId;
  }

  public void setMovimientoId(UUID movimientoId) {
    this.movimientoId = movimientoId;
  }

  public String getNumeroCuenta() {
    return numeroCuenta;
  }

  public void setNumeroCuenta(String numeroCuenta) {
    this.numeroCuenta = numeroCuenta;
  }

  public Instant getFecha() {
    return fecha;
  }

  public void setFecha(Instant fecha) {
    this.fecha = fecha;
  }

  public String getTipoMovimiento() {
    return tipoMovimiento;
  }

  public void setTipoMovimiento(String tipoMovimiento) {
    this.tipoMovimiento = tipoMovimiento;
  }

  public BigDecimal getValor() {
    return valor;
  }

  public void setValor(BigDecimal valor) {
    this.valor = valor;
  }

  public BigDecimal getSaldoAnterior() {
    return saldoAnterior;
  }

  public void setSaldoAnterior(BigDecimal saldoAnterior) {
    this.saldoAnterior = saldoAnterior;
  }

  public BigDecimal getSaldo() {
    return saldo;
  }

  public void setSaldo(BigDecimal saldo) {
    this.saldo = saldo;
  }
}
