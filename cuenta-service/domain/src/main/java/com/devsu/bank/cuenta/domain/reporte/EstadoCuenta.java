package com.devsu.bank.cuenta.domain.reporte;

import com.devsu.bank.cuenta.domain.model.ClienteProyeccion;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.Movimiento;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/** F4 "Estado de cuenta": the client's accounts with balances and the movement detail. */
public record EstadoCuenta(
    ClienteProyeccion cliente,
    RangoFechas rango,
    List<ResumenCuenta> cuentas,
    List<DetalleMovimiento> movimientos) {

  public static EstadoCuenta generar(
      ClienteProyeccion cliente, RangoFechas rango, List<CuentaConMovimientos> cuentas) {
    List<ResumenCuenta> resumenes = cuentas.stream().map(EstadoCuenta::resumir).toList();
    List<DetalleMovimiento> detalle =
        cuentas.stream()
            .flatMap(c -> c.movimientos().stream().map(m -> detallar(cliente, c.cuenta(), m)))
            .sorted(Comparator.comparing(DetalleMovimiento::fecha))
            .toList();
    return new EstadoCuenta(cliente, rango, resumenes, detalle);
  }

  private static ResumenCuenta resumir(CuentaConMovimientos item) {
    Cuenta cuenta = item.cuenta();
    return new ResumenCuenta(
        cuenta.getNumeroCuenta(),
        cuenta.getTipo(),
        cuenta.getSaldoInicial(),
        cuenta.getSaldoDisponible(),
        cuenta.isActiva(),
        total(item.movimientos(), m -> !m.valor().isNegative()),
        total(item.movimientos(), m -> m.valor().isNegative()));
  }

  private static Monto total(List<Movimiento> movimientos, Predicate<Movimiento> filtro) {
    return movimientos.stream()
        .filter(filtro)
        .map(m -> m.valor().abs())
        .reduce(Monto.ZERO, Monto::plus);
  }

  private static DetalleMovimiento detallar(
      ClienteProyeccion cliente, Cuenta cuenta, Movimiento movimiento) {
    return new DetalleMovimiento(
        movimiento.fecha(),
        cliente.nombre(),
        cuenta.getNumeroCuenta(),
        cuenta.getTipo(),
        movimiento.saldoAnterior(),
        cuenta.isActiva(),
        movimiento.valor(),
        movimiento.saldo());
  }
}
