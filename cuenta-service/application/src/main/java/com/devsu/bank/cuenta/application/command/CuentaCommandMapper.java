package com.devsu.bank.cuenta.application.command;

import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.TipoMovimiento;
import java.math.BigDecimal;

/** Command → domain conversions shared by the movement use cases. */
public final class CuentaCommandMapper {

  private CuentaCommandMapper() {}

  /** The sign decides the type; a declared type is accepted only when it agrees with it. */
  public static Monto valorMovimiento(BigDecimal valor, String tipoDeclarado) {
    Monto monto = Monto.of(valor);
    if (tipoDeclarado != null
        && TipoMovimiento.desde(tipoDeclarado) != TipoMovimiento.desde(monto)) {
      throw new DatoInvalidoException(
          "tipoMovimiento", "no coincide con el signo del valor (positivo = DEPOSITO)");
    }
    return monto;
  }
}
