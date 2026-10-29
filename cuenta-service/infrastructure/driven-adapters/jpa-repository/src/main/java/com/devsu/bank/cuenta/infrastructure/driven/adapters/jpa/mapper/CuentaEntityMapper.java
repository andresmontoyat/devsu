package com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.mapper;

import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.model.ClienteProyeccion;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.Movimiento;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.model.TipoCuenta;
import com.devsu.bank.cuenta.domain.model.TipoMovimiento;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.entity.ClienteProyeccionJpaEntity;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.entity.CuentaJpaEntity;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.entity.MovimientoJpaEntity;

/** Domain ↔ JPA entity. Entities never leave this module. */
public final class CuentaEntityMapper {

  private CuentaEntityMapper() {}

  public static Cuenta toDomain(CuentaJpaEntity e) {
    return Cuenta.builder()
        .numeroCuenta(new NumeroCuenta(e.getNumeroCuenta()))
        .tipo(TipoCuenta.valueOf(e.getTipoCuenta()))
        .saldoInicial(Monto.of(e.getSaldoInicial()))
        .saldoDisponible(Monto.of(e.getSaldoDisponible()))
        .activa(e.isEstado())
        .clienteId(new ClienteId(e.getClienteId()))
        .version(e.getVersion())
        .build();
  }

  public static CuentaJpaEntity copyInto(Cuenta cuenta, CuentaJpaEntity e) {
    e.setNumeroCuenta(cuenta.getNumeroCuenta().value());
    e.setTipoCuenta(cuenta.getTipo().name());
    e.setSaldoInicial(cuenta.getSaldoInicial().value());
    e.setSaldoDisponible(cuenta.getSaldoDisponible().value());
    e.setEstado(cuenta.isActiva());
    e.setClienteId(cuenta.getClienteId().value());
    return e;
  }

  public static Movimiento toDomain(MovimientoJpaEntity e) {
    return new Movimiento(
        new MovimientoId(e.getMovimientoId()),
        new NumeroCuenta(e.getNumeroCuenta()),
        e.getFecha(),
        TipoMovimiento.valueOf(e.getTipoMovimiento()),
        Monto.of(e.getValor()),
        Monto.of(e.getSaldoAnterior()),
        Monto.of(e.getSaldo()));
  }

  public static MovimientoJpaEntity copyInto(Movimiento m, MovimientoJpaEntity e) {
    e.setMovimientoId(m.id().value());
    e.setNumeroCuenta(m.numeroCuenta().value());
    e.setFecha(m.fecha());
    e.setTipoMovimiento(m.tipo().name());
    e.setValor(m.valor().value());
    e.setSaldoAnterior(m.saldoAnterior().value());
    e.setSaldo(m.saldo().value());
    return e;
  }

  public static ClienteProyeccion toDomain(ClienteProyeccionJpaEntity e) {
    return new ClienteProyeccion(
        new ClienteId(e.getClienteId()),
        e.getNombre(),
        e.isActivo(),
        e.isEliminado(),
        e.getVersion());
  }

  public static ClienteProyeccionJpaEntity copyInto(
      ClienteProyeccion p, ClienteProyeccionJpaEntity e) {
    e.setClienteId(p.clienteId().value());
    e.setNombre(p.nombre());
    e.setActivo(p.activo());
    e.setEliminado(p.eliminado());
    e.setVersion(p.version());
    return e;
  }
}
