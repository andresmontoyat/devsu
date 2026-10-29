package com.devsu.bank.cliente.config;

import com.devsu.bank.cliente.application.event.ClienteOutboxEvents;
import com.devsu.bank.cliente.application.port.inbound.CreateClientePort;
import com.devsu.bank.cliente.application.port.inbound.DeleteClientePort;
import com.devsu.bank.cliente.application.port.inbound.GetClientePort;
import com.devsu.bank.cliente.application.port.inbound.ListClientesPort;
import com.devsu.bank.cliente.application.port.inbound.PatchClientePort;
import com.devsu.bank.cliente.application.port.inbound.UpdateClientePort;
import com.devsu.bank.cliente.application.port.outbound.ClienteRepositoryPort;
import com.devsu.bank.cliente.application.port.outbound.PasswordHasherPort;
import com.devsu.bank.cliente.application.port.outbound.TransactionPort;
import com.devsu.bank.cliente.application.usecase.CreateClienteUseCase;
import com.devsu.bank.cliente.application.usecase.DeleteClienteUseCase;
import com.devsu.bank.cliente.application.usecase.GetClienteUseCase;
import com.devsu.bank.cliente.application.usecase.ListClientesUseCase;
import com.devsu.bank.cliente.application.usecase.PatchClienteUseCase;
import com.devsu.bank.cliente.application.usecase.UpdateClienteUseCase;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.infrastructure.driven.adapters.metrics.support.ChainablePort;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/** The only place that names use case implementations: each port is exposed as its chain head. */
@Configuration(proxyBeanMethods = false)
public class UseCaseConfig {

  @Bean
  @Primary
  CreateClientePort createClientePort(
      TransactionPort<Cliente> transaction,
      ClienteRepositoryPort repository,
      PasswordHasherPort hasher,
      ClienteOutboxEvents events,
      List<ChainablePort<?>> links) {
    return Chains.build(
        CreateClientePort.class,
        new CreateClienteUseCase(transaction, repository, hasher, events),
        links);
  }

  @Bean
  @Primary
  GetClientePort getClientePort(ClienteRepositoryPort repository, List<ChainablePort<?>> links) {
    return Chains.build(GetClientePort.class, new GetClienteUseCase(repository), links);
  }

  @Bean
  @Primary
  ListClientesPort listClientesPort(
      ClienteRepositoryPort repository, List<ChainablePort<?>> links) {
    return Chains.build(ListClientesPort.class, new ListClientesUseCase(repository), links);
  }

  @Bean
  @Primary
  UpdateClientePort updateClientePort(
      TransactionPort<Cliente> transaction,
      ClienteRepositoryPort repository,
      PasswordHasherPort hasher,
      ClienteOutboxEvents events,
      List<ChainablePort<?>> links) {
    return Chains.build(
        UpdateClientePort.class,
        new UpdateClienteUseCase(transaction, repository, hasher, events),
        links);
  }

  @Bean
  @Primary
  PatchClientePort patchClientePort(
      TransactionPort<Cliente> transaction,
      ClienteRepositoryPort repository,
      PasswordHasherPort hasher,
      ClienteOutboxEvents events,
      List<ChainablePort<?>> links) {
    return Chains.build(
        PatchClientePort.class,
        new PatchClienteUseCase(transaction, repository, hasher, events),
        links);
  }

  @Bean
  @Primary
  DeleteClientePort deleteClientePort(
      TransactionPort<Void> transaction,
      ClienteRepositoryPort repository,
      ClienteOutboxEvents events,
      List<ChainablePort<?>> links) {
    return Chains.build(
        DeleteClientePort.class, new DeleteClienteUseCase(transaction, repository, events), links);
  }
}
