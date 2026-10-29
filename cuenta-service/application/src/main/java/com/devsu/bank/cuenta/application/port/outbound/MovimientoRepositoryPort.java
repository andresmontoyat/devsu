package com.devsu.bank.cuenta.application.port.outbound;

import com.devsu.bank.cuenta.application.query.MovimientoFiltro;
import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.application.query.PaginaSolicitud;
import com.devsu.bank.cuenta.domain.model.Movimiento;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MovimientoRepositoryPort {

  /** Inserts a new movement or overwrites a corrected one with the same id. */
  Movimiento save(Movimiento movimiento);

  Optional<Movimiento> findById(MovimientoId id);

  Optional<Movimiento> findLatestByCuenta(NumeroCuenta numeroCuenta);

  boolean existsByCuenta(NumeroCuenta numeroCuenta);

  Pagina<Movimiento> findAll(MovimientoFiltro filtro, PaginaSolicitud solicitud);

  /** One query for every account of a statement: [desde, hastaExclusivo), oldest first. */
  List<Movimiento> findByCuentasBetween(
      List<NumeroCuenta> cuentas, Instant desde, Instant hastaExclusivo);
}
