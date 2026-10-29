package com.devsu.bank.cuenta.application.support;

import com.devsu.bank.cuenta.application.port.outbound.MovimientoRepositoryPort;
import com.devsu.bank.cuenta.application.query.MovimientoFiltro;
import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.application.query.PaginaSolicitud;
import com.devsu.bank.cuenta.domain.model.Movimiento;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/** Insertion order stands for chronological order, as the ledger sequence does in the database. */
public final class InMemoryMovimientoRepository implements MovimientoRepositoryPort {

  private final Map<MovimientoId, Movimiento> movimientos = new LinkedHashMap<>();

  @Override
  public Movimiento save(Movimiento movimiento) {
    movimientos.put(movimiento.id(), movimiento);
    return movimiento;
  }

  @Override
  public Optional<Movimiento> findById(MovimientoId id) {
    return Optional.ofNullable(movimientos.get(id));
  }

  @Override
  public Optional<Movimiento> findLatestByCuenta(NumeroCuenta numeroCuenta) {
    return de(numeroCuenta).reduce((primero, segundo) -> segundo);
  }

  @Override
  public boolean existsByCuenta(NumeroCuenta numeroCuenta) {
    return de(numeroCuenta).findAny().isPresent();
  }

  @Override
  public Pagina<Movimiento> findAll(MovimientoFiltro filtro, PaginaSolicitud solicitud) {
    List<Movimiento> filtrados = movimientos.values().stream().filter(cumple(filtro)).toList();
    int desde = Math.min(solicitud.offset(), filtrados.size());
    int hasta = Math.min(desde + solicitud.tamano(), filtrados.size());
    return new Pagina<>(filtrados.subList(desde, hasta), solicitud, filtrados.size());
  }

  @Override
  public List<Movimiento> findByCuentasBetween(
      List<NumeroCuenta> cuentas, Instant desde, Instant hastaExclusivo) {
    return movimientos.values().stream()
        .filter(m -> cuentas.contains(m.numeroCuenta()))
        .filter(cumple(new MovimientoFiltro(null, desde, hastaExclusivo)))
        .toList();
  }

  public int cantidad() {
    return movimientos.size();
  }

  private java.util.stream.Stream<Movimiento> de(NumeroCuenta numeroCuenta) {
    return movimientos.values().stream().filter(m -> m.numeroCuenta().equals(numeroCuenta));
  }

  private static Predicate<Movimiento> cumple(MovimientoFiltro filtro) {
    return m ->
        (filtro.numeroCuenta() == null || filtro.numeroCuenta().equals(m.numeroCuenta()))
            && (filtro.desde() == null || !m.fecha().isBefore(filtro.desde()))
            && (filtro.hastaExclusivo() == null || m.fecha().isBefore(filtro.hastaExclusivo()));
  }
}
