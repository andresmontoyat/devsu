package com.devsu.bank.cuenta.application.usecase;

import com.devsu.bank.cuenta.application.port.inbound.GenerateEstadoCuentaPort;
import com.devsu.bank.cuenta.application.port.outbound.ClienteProyeccionRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.CuentaRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.MovimientoRepositoryPort;
import com.devsu.bank.cuenta.application.query.EstadoCuentaQuery;
import com.devsu.bank.cuenta.domain.exception.ClienteNoEncontradoException;
import com.devsu.bank.cuenta.domain.model.ClienteProyeccion;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.Movimiento;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.reporte.CuentaConMovimientos;
import com.devsu.bank.cuenta.domain.reporte.EstadoCuenta;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * F4. Reads only local data (the client name comes from the projection) and fetches the movements
 * of all the client's accounts in a single query — no N+1.
 */
public final class GenerateEstadoCuentaUseCase implements GenerateEstadoCuentaPort {

  private final ClienteProyeccionRepositoryPort clientes;
  private final CuentaRepositoryPort cuentas;
  private final MovimientoRepositoryPort movimientos;
  private final ZoneId zona;

  public GenerateEstadoCuentaUseCase(
      ClienteProyeccionRepositoryPort clientes,
      CuentaRepositoryPort cuentas,
      MovimientoRepositoryPort movimientos,
      ZoneId zona) {
    this.clientes = clientes;
    this.cuentas = cuentas;
    this.movimientos = movimientos;
    this.zona = zona;
  }

  @Override
  public EstadoCuenta execute(EstadoCuentaQuery query) {
    ClienteProyeccion cliente =
        clientes
            .findById(query.clienteId())
            .orElseThrow(() -> new ClienteNoEncontradoException(query.clienteId()));
    List<Cuenta> delCliente = cuentas.findByClienteId(query.clienteId());
    Map<NumeroCuenta, List<Movimiento>> porCuenta = movimientosPorCuenta(delCliente, query);
    List<CuentaConMovimientos> detalle =
        delCliente.stream()
            .map(
                c ->
                    new CuentaConMovimientos(
                        c, porCuenta.getOrDefault(c.getNumeroCuenta(), List.of())))
            .toList();
    return EstadoCuenta.generar(cliente, query.rango(), detalle);
  }

  private Map<NumeroCuenta, List<Movimiento>> movimientosPorCuenta(
      List<Cuenta> delCliente, EstadoCuentaQuery query) {
    List<NumeroCuenta> numeros = delCliente.stream().map(Cuenta::getNumeroCuenta).toList();
    if (numeros.isEmpty()) {
      return Map.of();
    }
    return movimientos
        .findByCuentasBetween(numeros, query.rango().inicio(zona), query.rango().finExclusivo(zona))
        .stream()
        .collect(Collectors.groupingBy(Movimiento::numeroCuenta));
  }
}
