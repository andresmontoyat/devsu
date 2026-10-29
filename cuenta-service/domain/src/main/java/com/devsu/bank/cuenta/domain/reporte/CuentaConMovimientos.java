package com.devsu.bank.cuenta.domain.reporte;

import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.Movimiento;
import java.util.List;

/** An account with its movements inside the requested range. */
public record CuentaConMovimientos(Cuenta cuenta, List<Movimiento> movimientos) {

  public CuentaConMovimientos {
    movimientos = List.copyOf(movimientos);
  }
}
