package com.devsu.bank.cuenta.application.usecase;

import com.devsu.bank.cuenta.application.port.inbound.GetMovimientoPort;
import com.devsu.bank.cuenta.application.port.outbound.MovimientoRepositoryPort;
import com.devsu.bank.cuenta.domain.model.Movimiento;
import com.devsu.bank.cuenta.domain.model.MovimientoId;

public final class GetMovimientoUseCase implements GetMovimientoPort {

  private final MovimientoRepositoryPort movimientos;

  public GetMovimientoUseCase(MovimientoRepositoryPort movimientos) {
    this.movimientos = movimientos;
  }

  @Override
  public Movimiento execute(MovimientoId movimientoId) {
    return Busquedas.movimiento(movimientos, movimientoId);
  }
}
