package com.devsu.bank.cliente.config;

import com.devsu.bank.cliente.application.port.outbound.TransactionPort;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.infrastructure.driven.adapters.jpa.adapter.JpaTransactionAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
public class PersistenceConfig {

  @Bean
  TransactionPort<Cliente> clienteTransaction(TransactionTemplate transactionTemplate) {
    return new JpaTransactionAdapter<>(transactionTemplate);
  }

  @Bean
  TransactionPort<Void> voidTransaction(TransactionTemplate transactionTemplate) {
    return new JpaTransactionAdapter<>(transactionTemplate);
  }
}
