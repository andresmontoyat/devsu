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
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.ChainablePort;
import java.time.Clock;
import java.time.ZoneId;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/** The only place that names use case implementations: each port is exposed as its chain head. */
@Configuration(proxyBeanMethods = false)
public class UseCaseConfig {

  @Bean
  @Primary
  CreateCuentaPort createCuentaPort(
      TransactionPort<Cuenta> transaction,
      CuentaRepositoryPort cuentas,
      ClienteProyeccionRepositoryPort clientes,
      List<ChainablePort<?>> links) {
    return Chains.build(
        CreateCuentaPort.class, new CreateCuentaUseCase(transaction, cuentas, clientes), links);
  }

  @Bean
  @Primary
  GetCuentaPort getCuentaPort(CuentaRepositoryPort cuentas, List<ChainablePort<?>> links) {
    return Chains.build(GetCuentaPort.class, new GetCuentaUseCase(cuentas), links);
  }

  @Bean
  @Primary
  ListCuentasPort listCuentasPort(CuentaRepositoryPort cuentas, List<ChainablePort<?>> links) {
    return Chains.build(ListCuentasPort.class, new ListCuentasUseCase(cuentas), links);
  }

  @Bean
  @Primary
  UpdateCuentaPort updateCuentaPort(
      TransactionPort<Cuenta> transaction,
      CuentaRepositoryPort cuentas,
      MovimientoRepositoryPort movimientos,
      List<ChainablePort<?>> links) {
    return Chains.build(
        UpdateCuentaPort.class, new UpdateCuentaUseCase(transaction, cuentas, movimientos), links);
  }

  @Bean
  @Primary
  PatchCuentaPort patchCuentaPort(
      TransactionPort<Cuenta> transaction,
      CuentaRepositoryPort cuentas,
      List<ChainablePort<?>> links) {
    return Chains.build(PatchCuentaPort.class, new PatchCuentaUseCase(transaction, cuentas), links);
  }

  /** The idempotency decorator sits in front of the use case and outside its transaction. */
  @Bean
  @Primary
  RegisterMovimientoPort registerMovimientoPort(
      TransactionPort<ResultadoMovimiento> transaction,
      CuentaRepositoryPort cuentas,
      MovimientoRepositoryPort movimientos,
      IdempotencyStorePort idempotencyStore,
      Clock clock,
      List<ChainablePort<?>> links) {
    RegisterMovimientoUseCase core =
        new RegisterMovimientoUseCase(transaction, cuentas, movimientos, clock);
    return Chains.build(
        RegisterMovimientoPort.class,
        new IdempotentRegisterMovimientoUseCase(core, idempotencyStore, movimientos, cuentas),
        links);
  }

  @Bean
  @Primary
  CorrectMovimientoPort correctMovimientoPort(
      TransactionPort<ResultadoMovimiento> transaction,
      CuentaRepositoryPort cuentas,
      MovimientoRepositoryPort movimientos,
      List<ChainablePort<?>> links) {
    return Chains.build(
        CorrectMovimientoPort.class,
        new CorrectMovimientoUseCase(transaction, cuentas, movimientos),
        links);
  }

  @Bean
  @Primary
  GetMovimientoPort getMovimientoPort(
      MovimientoRepositoryPort movimientos, List<ChainablePort<?>> links) {
    return Chains.build(GetMovimientoPort.class, new GetMovimientoUseCase(movimientos), links);
  }

  @Bean
  @Primary
  ListMovimientosPort listMovimientosPort(
      MovimientoRepositoryPort movimientos, ZoneId zona, List<ChainablePort<?>> links) {
    return Chains.build(
        ListMovimientosPort.class, new ListMovimientosUseCase(movimientos, zona), links);
  }

  @Bean
  @Primary
  GenerateEstadoCuentaPort generateEstadoCuentaPort(
      ClienteProyeccionRepositoryPort clientes,
      CuentaRepositoryPort cuentas,
      MovimientoRepositoryPort movimientos,
      ZoneId zona,
      List<ChainablePort<?>> links) {
    return Chains.build(
        GenerateEstadoCuentaPort.class,
        new GenerateEstadoCuentaUseCase(clientes, cuentas, movimientos, zona),
        links);
  }

  @Bean
  @Primary
  ProjectClienteEventPort projectClienteEventPort(
      TransactionPort<Void> transaction,
      ClienteProyeccionRepositoryPort clientes,
      CuentaRepositoryPort cuentas,
      ProcessedEventRepositoryPort procesados,
      List<ChainablePort<?>> links) {
    return Chains.build(
        ProjectClienteEventPort.class,
        new ProjectClienteEventUseCase(transaction, clientes, cuentas, procesados),
        links);
  }
}
