package com.devsu.bank.cuenta.application.usecase;

import com.devsu.bank.cuenta.application.command.CorrectMovimientoCommand;
import com.devsu.bank.cuenta.application.command.CuentaCommandMapper;
import com.devsu.bank.cuenta.application.port.inbound.CorrectMovimientoPort;
import com.devsu.bank.cuenta.application.port.outbound.CuentaRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.MovimientoRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.TransactionPort;
import com.devsu.bank.cuenta.domain.exception.MovimientoNoModificableException;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.Movimiento;
import com.devsu.bank.cuenta.domain.model.ResultadoMovimiento;

/**
 * The "U" of movements. The ledger is append-only except for its tail: changing an older entry
 * would invalidate every balance recorded after it, so only the latest one may be corrected.
 */
public final class CorrectMovimientoUseCase implements CorrectMovimientoPort {

  private final TransactionPort<ResultadoMovimiento> transaction;
  private final CuentaRepositoryPort cuentas;
  private final MovimientoRepositoryPort movimientos;

  public CorrectMovimientoUseCase(
      TransactionPort<ResultadoMovimiento> transaction,
      CuentaRepositoryPort cuentas,
      MovimientoRepositoryPort movimientos) {
    this.transaction = transaction;
    this.cuentas = cuentas;
    this.movimientos = movimientos;
  }

  @Override
  public ResultadoMovimiento execute(CorrectMovimientoCommand command) {
    Monto valor = CuentaCommandMapper.valorMovimiento(command.valor(), command.tipoMovimiento());
    return transaction.run(
        () -> {
          Movimiento movimiento = Busquedas.movimiento(movimientos, command.movimientoId());
          Cuenta cuenta = Busquedas.cuentaBloqueada(cuentas, movimiento.numeroCuenta());
          exigirUltimo(movimiento);
          ResultadoMovimiento corregido = cuenta.corregirUltimoMovimiento(movimiento, valor);
          return new ResultadoMovimiento(
              cuentas.update(corregido.cuenta()), movimientos.save(corregido.movimiento()));
        });
  }

  /** Checked after the account lock, so no movement can be appended in between. */
  private void exigirUltimo(Movimiento movimiento) {
    boolean esUltimo =
        movimientos
            .findLatestByCuenta(movimiento.numeroCuenta())
            .map(ultimo -> ultimo.id().equals(movimiento.id()))
            .orElse(false);
    if (!esUltimo) {
      throw new MovimientoNoModificableException(movimiento.id());
    }
  }
}
