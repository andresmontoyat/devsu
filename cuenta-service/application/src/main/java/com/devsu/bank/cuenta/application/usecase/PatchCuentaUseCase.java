package com.devsu.bank.cuenta.application.usecase;

import com.devsu.bank.cuenta.application.command.PatchCuentaCommand;
import com.devsu.bank.cuenta.application.port.inbound.PatchCuentaPort;
import com.devsu.bank.cuenta.application.port.outbound.CuentaRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.TransactionPort;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.model.TipoCuenta;

public final class PatchCuentaUseCase implements PatchCuentaPort {

  private final TransactionPort<Cuenta> transaction;
  private final CuentaRepositoryPort cuentas;

  public PatchCuentaUseCase(TransactionPort<Cuenta> transaction, CuentaRepositoryPort cuentas) {
    this.transaction = transaction;
    this.cuentas = cuentas;
  }

  @Override
  public Cuenta execute(PatchCuentaCommand command) {
    NumeroCuenta numero = new NumeroCuenta(command.numeroCuenta());
    TipoCuenta tipo = command.tipoCuenta() == null ? null : TipoCuenta.desde(command.tipoCuenta());
    return transaction.run(
        () -> {
          Cuenta actual = Busquedas.cuentaBloqueada(cuentas, numero);
          Cuenta tipada = tipo == null ? actual : actual.cambiarTipo(tipo);
          return cuentas.update(conEstado(tipada, command.estado()));
        });
  }

  private static Cuenta conEstado(Cuenta cuenta, Boolean activa) {
    if (activa == null) {
      return cuenta;
    }
    return activa ? cuenta.activar() : cuenta.desactivar();
  }
}
