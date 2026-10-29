package com.devsu.bank.cuenta.application.usecase;

import com.devsu.bank.cuenta.application.command.CuentaCommandMapper;
import com.devsu.bank.cuenta.application.command.RegisterMovimientoCommand;
import com.devsu.bank.cuenta.application.port.inbound.RegisterMovimientoPort;
import com.devsu.bank.cuenta.application.port.outbound.CuentaRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.MovimientoRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.TransactionPort;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.model.ResultadoMovimiento;
import java.time.Clock;

/**
 * F2/F3. The account row is locked for the whole transaction, so concurrent movements on the same
 * account are serialized and the balance check cannot be raced.
 */
public final class RegisterMovimientoUseCase implements RegisterMovimientoPort {

  private final TransactionPort<ResultadoMovimiento> transaction;
  private final CuentaRepositoryPort cuentas;
  private final MovimientoRepositoryPort movimientos;
  private final Clock clock;

  public RegisterMovimientoUseCase(
      TransactionPort<ResultadoMovimiento> transaction,
      CuentaRepositoryPort cuentas,
      MovimientoRepositoryPort movimientos,
      Clock clock) {
    this.transaction = transaction;
    this.cuentas = cuentas;
    this.movimientos = movimientos;
    this.clock = clock;
  }

  @Override
  public ResultadoMovimiento execute(RegisterMovimientoCommand command) {
    NumeroCuenta numero = new NumeroCuenta(command.numeroCuenta());
    Monto valor = CuentaCommandMapper.valorMovimiento(command.valor(), command.tipoMovimiento());
    return transaction.run(
        () -> {
          Cuenta cuenta = Busquedas.cuentaBloqueada(cuentas, numero);
          ResultadoMovimiento resultado =
              cuenta.aplicarMovimiento(MovimientoId.generar(), valor, clock.instant());
          return guardar(resultado);
        });
  }

  private ResultadoMovimiento guardar(ResultadoMovimiento resultado) {
    Cuenta cuenta = cuentas.save(resultado.cuenta());
    return new ResultadoMovimiento(cuenta, movimientos.save(resultado.movimiento()));
  }
}
