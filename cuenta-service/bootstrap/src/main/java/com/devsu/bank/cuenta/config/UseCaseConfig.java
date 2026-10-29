package com.devsu.bank.cuenta.config;

import com.devsu.bank.cuenta.application.port.inbound.CorrectMovimientoPort;
import com.devsu.bank.cuenta.application.port.inbound.CreateCuentaPort;
import com.devsu.bank.cuenta.application.port.inbound.GenerateEstadoCuentaPort;
import com.devsu.bank.cuenta.application.port.inbound.GetCuentaPort;
import com.devsu.bank.cuenta.application.port.inbound.GetMovimientoPort;
import com.devsu.bank.cuenta.application.port.inbound.ListCuentasPort;
import com.devsu.bank.cuenta.application.port.inbound.ListMovimientosPort;
import com.devsu.bank.cuenta.application.port.inbound.PatchCuentaPort;
import com.devsu.bank.cuenta.application.port.inbound.ProjectClienteEventPort;
import com.devsu.bank.cuenta.application.port.inbound.RegisterMovimientoPort;
import com.devsu.bank.cuenta.application.port.inbound.UpdateCuentaPort;
import com.devsu.bank.cuenta.application.port.outbound.ClienteProyeccionRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.CuentaRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.IdempotencyStorePort;
import com.devsu.bank.cuenta.application.port.outbound.MovimientoRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.ProcessedEventRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.TransactionPort;
import com.devsu.bank.cuenta.application.usecase.CorrectMovimientoUseCase;
import com.devsu.bank.cuenta.application.usecase.CreateCuentaUseCase;
import com.devsu.bank.cuenta.application.usecase.GenerateEstadoCuentaUseCase;
import com.devsu.bank.cuenta.application.usecase.GetCuentaUseCase;
import com.devsu.bank.cuenta.application.usecase.GetMovimientoUseCase;
import com.devsu.bank.cuenta.application.usecase.IdempotentRegisterMovimientoUseCase;
import com.devsu.bank.cuenta.application.usecase.ListCuentasUseCase;
import com.devsu.bank.cuenta.application.usecase.ListMovimientosUseCase;
import com.devsu.bank.cuenta.application.usecase.PatchCuentaUseCase;
import com.devsu.bank.cuenta.application.usecase.ProjectClienteEventUseCase;
import com.devsu.bank.cuenta.application.usecase.RegisterMovimientoUseCase;
import com.devsu.bank.cuenta.application.usecase.UpdateCuentaUseCase;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.ResultadoMovimiento;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** The only place that names use case implementations. */
@Configuration(proxyBeanMethods = false)
public class UseCaseConfig {

  @Bean
  CreateCuentaPort createCuentaPort(
      TransactionPort<Cuenta> transaction,
      CuentaRepositoryPort cuentas,
      ClienteProyeccionRepositoryPort clientes) {
    return new CreateCuentaUseCase(transaction, cuentas, clientes);
  }

  @Bean
  GetCuentaPort getCuentaPort(CuentaRepositoryPort cuentas) {
    return new GetCuentaUseCase(cuentas);
  }

  @Bean
  ListCuentasPort listCuentasPort(CuentaRepositoryPort cuentas) {
    return new ListCuentasUseCase(cuentas);
  }

  @Bean
  UpdateCuentaPort updateCuentaPort(
      TransactionPort<Cuenta> transaction,
      CuentaRepositoryPort cuentas,
      MovimientoRepositoryPort movimientos) {
    return new UpdateCuentaUseCase(transaction, cuentas, movimientos);
  }

  @Bean
  PatchCuentaPort patchCuentaPort(
      TransactionPort<Cuenta> transaction, CuentaRepositoryPort cuentas) {
    return new PatchCuentaUseCase(transaction, cuentas);
  }

  /** The idempotency decorator sits in front of the use case and outside its transaction. */
  @Bean
  RegisterMovimientoPort registerMovimientoPort(
      TransactionPort<ResultadoMovimiento> transaction,
      CuentaRepositoryPort cuentas,
      MovimientoRepositoryPort movimientos,
      IdempotencyStorePort idempotencyStore,
      Clock clock) {
    RegisterMovimientoUseCase core =
        new RegisterMovimientoUseCase(transaction, cuentas, movimientos, clock);
    return new IdempotentRegisterMovimientoUseCase(core, idempotencyStore, movimientos, cuentas);
  }

  @Bean
  CorrectMovimientoPort correctMovimientoPort(
      TransactionPort<ResultadoMovimiento> transaction,
      CuentaRepositoryPort cuentas,
      MovimientoRepositoryPort movimientos) {
    return new CorrectMovimientoUseCase(transaction, cuentas, movimientos);
  }

  @Bean
  GetMovimientoPort getMovimientoPort(MovimientoRepositoryPort movimientos) {
    return new GetMovimientoUseCase(movimientos);
  }

  @Bean
  ListMovimientosPort listMovimientosPort(MovimientoRepositoryPort movimientos, ZoneId zona) {
    return new ListMovimientosUseCase(movimientos, zona);
  }

  @Bean
  GenerateEstadoCuentaPort generateEstadoCuentaPort(
      ClienteProyeccionRepositoryPort clientes,
      CuentaRepositoryPort cuentas,
      MovimientoRepositoryPort movimientos,
      ZoneId zona) {
    return new GenerateEstadoCuentaUseCase(clientes, cuentas, movimientos, zona);
  }

  @Bean
  ProjectClienteEventPort projectClienteEventPort(
      TransactionPort<Void> transaction,
      ClienteProyeccionRepositoryPort clientes,
      CuentaRepositoryPort cuentas,
      ProcessedEventRepositoryPort procesados) {
    return new ProjectClienteEventUseCase(transaction, clientes, cuentas, procesados);
  }
}
