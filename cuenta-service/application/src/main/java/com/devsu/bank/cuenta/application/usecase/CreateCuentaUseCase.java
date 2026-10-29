package com.devsu.bank.cuenta.application.usecase;

import com.devsu.bank.cuenta.application.command.CreateCuentaCommand;
import com.devsu.bank.cuenta.application.port.inbound.CreateCuentaPort;
import com.devsu.bank.cuenta.application.port.outbound.ClienteProyeccionRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.CuentaRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.TransactionPort;
import com.devsu.bank.cuenta.domain.exception.ClienteNoDisponibleException;
import com.devsu.bank.cuenta.domain.exception.NumeroCuentaDuplicadoException;
import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.model.TipoCuenta;

/**
 * Opens an account for a client known through the local projection: no synchronous call to
 * cliente-service, so this service keeps working while the other one is down.
 */
public final class CreateCuentaUseCase implements CreateCuentaPort {

  private final TransactionPort<Cuenta> transaction;
  private final CuentaRepositoryPort cuentas;
  private final ClienteProyeccionRepositoryPort clientes;

  public CreateCuentaUseCase(
      TransactionPort<Cuenta> transaction,
      CuentaRepositoryPort cuentas,
      ClienteProyeccionRepositoryPort clientes) {
    this.transaction = transaction;
    this.cuentas = cuentas;
    this.clientes = clientes;
  }

  @Override
  public Cuenta execute(CreateCuentaCommand command) {
    ClienteId clienteId = ClienteId.of(command.clienteId());
    Cuenta nueva =
        Cuenta.abrir(
            new NumeroCuenta(command.numeroCuenta()),
            TipoCuenta.desde(command.tipoCuenta()),
            Monto.of(command.saldoInicial()),
            clienteId);
    Cuenta conEstado = Boolean.FALSE.equals(command.estado()) ? nueva.desactivar() : nueva;
    return transaction.run(() -> abrir(conEstado));
  }

  private Cuenta abrir(Cuenta cuenta) {
    clientes
        .findById(cuenta.getClienteId())
        .orElseThrow(() -> new ClienteNoDisponibleException(cuenta.getClienteId()))
        .exigirDisponible();
    if (cuentas.existsByNumero(cuenta.getNumeroCuenta())) {
      throw new NumeroCuentaDuplicadoException(cuenta.getNumeroCuenta());
    }
    return cuentas.save(cuenta);
  }
}
