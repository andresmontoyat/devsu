package com.devsu.bank.cuenta.application.usecase;

import com.devsu.bank.cuenta.application.command.UpdateCuentaCommand;
import com.devsu.bank.cuenta.application.port.inbound.UpdateCuentaPort;
import com.devsu.bank.cuenta.application.port.outbound.CuentaRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.MovimientoRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.TransactionPort;
import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import com.devsu.bank.cuenta.domain.exception.SaldoInicialNoModificableException;
import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.model.TipoCuenta;

/** PUT: type and state are free; the owner never changes; the initial balance only before use. */
public final class UpdateCuentaUseCase implements UpdateCuentaPort {

  private final TransactionPort<Cuenta> transaction;
  private final CuentaRepositoryPort cuentas;
  private final MovimientoRepositoryPort movimientos;

  public UpdateCuentaUseCase(
      TransactionPort<Cuenta> transaction,
      CuentaRepositoryPort cuentas,
      MovimientoRepositoryPort movimientos) {
    this.transaction = transaction;
    this.cuentas = cuentas;
    this.movimientos = movimientos;
  }

  @Override
  public Cuenta execute(UpdateCuentaCommand command) {
    NumeroCuenta numero = new NumeroCuenta(command.numeroCuenta());
    TipoCuenta tipo = TipoCuenta.desde(command.tipoCuenta());
    Monto saldoInicial = Monto.of(command.saldoInicial());
    ClienteId titular = ClienteId.of(command.clienteId());
    return transaction.run(
        () -> {
          Cuenta actual = Busquedas.cuentaBloqueada(cuentas, numero);
          exigirMismoTitular(actual, titular);
          Cuenta conSaldo = conSaldoInicial(actual, saldoInicial);
          Cuenta tipada = conSaldo.cambiarTipo(tipo);
          return cuentas.save(command.estado() ? tipada.activar() : tipada.desactivar());
        });
  }

  private Cuenta conSaldoInicial(Cuenta actual, Monto saldoInicial) {
    if (saldoInicial.equals(actual.getSaldoInicial())) {
      return actual;
    }
    if (movimientos.existsByCuenta(actual.getNumeroCuenta())) {
      throw new SaldoInicialNoModificableException(actual.getNumeroCuenta());
    }
    return actual.redefinirSaldoInicial(saldoInicial);
  }

  private static void exigirMismoTitular(Cuenta actual, ClienteId titular) {
    if (!actual.getClienteId().equals(titular)) {
      throw new DatoInvalidoException("clienteId", "el titular de una cuenta no puede cambiar");
    }
  }
}
