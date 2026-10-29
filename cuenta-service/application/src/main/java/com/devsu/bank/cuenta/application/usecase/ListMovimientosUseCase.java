package com.devsu.bank.cuenta.application.usecase;

import com.devsu.bank.cuenta.application.port.inbound.ListMovimientosPort;
import com.devsu.bank.cuenta.application.port.outbound.MovimientoRepositoryPort;
import com.devsu.bank.cuenta.application.query.MovimientoFiltro;
import com.devsu.bank.cuenta.application.query.MovimientosQuery;
import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.domain.model.Movimiento;
import com.devsu.bank.cuenta.domain.reporte.RangoFechas;
import java.time.ZoneId;

/** Calendar days are interpreted in the bank's zone, not in UTC. */
public final class ListMovimientosUseCase implements ListMovimientosPort {

  private final MovimientoRepositoryPort movimientos;
  private final ZoneId zona;

  public ListMovimientosUseCase(MovimientoRepositoryPort movimientos, ZoneId zona) {
    this.movimientos = movimientos;
    this.zona = zona;
  }

  @Override
  public Pagina<Movimiento> execute(MovimientosQuery query) {
    RangoFechas rango = query.rango();
    MovimientoFiltro filtro =
        new MovimientoFiltro(
            query.numeroCuenta(),
            rango == null ? null : rango.inicio(zona),
            rango == null ? null : rango.finExclusivo(zona));
    return movimientos.findAll(filtro, query.solicitud());
  }
}
