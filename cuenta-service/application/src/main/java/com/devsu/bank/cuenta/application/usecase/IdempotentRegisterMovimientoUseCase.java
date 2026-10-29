package com.devsu.bank.cuenta.application.usecase;

import com.devsu.bank.cuenta.application.command.RegisterMovimientoCommand;
import com.devsu.bank.cuenta.application.idempotency.IdempotencyKeyEnCursoException;
import com.devsu.bank.cuenta.application.idempotency.IdempotencyKeyReutilizadaException;
import com.devsu.bank.cuenta.application.idempotency.IdempotencyOutcome;
import com.devsu.bank.cuenta.application.port.inbound.RegisterMovimientoPort;
import com.devsu.bank.cuenta.application.port.outbound.CuentaRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.IdempotencyStorePort;
import com.devsu.bank.cuenta.application.port.outbound.MovimientoRepositoryPort;
import com.devsu.bank.cuenta.domain.model.Movimiento;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import com.devsu.bank.cuenta.domain.model.ResultadoMovimiento;

/**
 * Makes POST /movimientos safe to retry (api-design.md §10): a client that times out and resends
 * the same {@code Idempotency-Key} gets the original movement instead of a second debit. The key is
 * reserved before — and outside — the business transaction.
 */
public final class IdempotentRegisterMovimientoUseCase implements RegisterMovimientoPort {

  private final RegisterMovimientoPort delegate;
  private final IdempotencyStorePort store;
  private final MovimientoRepositoryPort movimientos;
  private final CuentaRepositoryPort cuentas;

  public IdempotentRegisterMovimientoUseCase(
      RegisterMovimientoPort delegate,
      IdempotencyStorePort store,
      MovimientoRepositoryPort movimientos,
      CuentaRepositoryPort cuentas) {
    this.delegate = delegate;
    this.store = store;
    this.movimientos = movimientos;
    this.cuentas = cuentas;
  }

  @Override
  public ResultadoMovimiento execute(RegisterMovimientoCommand command) {
    if (command.idempotencyKey() == null) {
      return delegate.execute(command);
    }
    return switch (store.reserve(command.idempotencyKey(), command.fingerprint())) {
      case IdempotencyOutcome.Reserved r -> ejecutarUnaVez(command);
      case IdempotencyOutcome.Replay replay -> original(replay.movimientoId());
      case IdempotencyOutcome.InProgress p -> throw new IdempotencyKeyEnCursoException();
      case IdempotencyOutcome.FingerprintMismatch m ->
          throw new IdempotencyKeyReutilizadaException();
    };
  }

  private ResultadoMovimiento ejecutarUnaVez(RegisterMovimientoCommand command) {
    try {
      ResultadoMovimiento resultado = delegate.execute(command);
      store.complete(command.idempotencyKey(), command.fingerprint(), resultado.movimiento().id());
      return resultado;
    } catch (RuntimeException e) {
      store.release(command.idempotencyKey());
      throw e;
    }
  }

  private ResultadoMovimiento original(MovimientoId id) {
    Movimiento movimiento = Busquedas.movimiento(movimientos, id);
    return new ResultadoMovimiento(
        Busquedas.cuenta(cuentas, movimiento.numeroCuenta()), movimiento);
  }
}
