package com.devsu.bank.cuenta.config;

import com.devsu.bank.cuenta.application.port.outbound.TransactionPort;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.ResultadoMovimiento;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.adapter.JpaTransactionAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
public class PersistenceConfig {

  @Bean
  TransactionPort<Cuenta> cuentaTransaction(TransactionTemplate transactionTemplate) {
    return new JpaTransactionAdapter<>(transactionTemplate);
  }

  @Bean
  TransactionPort<ResultadoMovimiento> movimientoTransaction(
      TransactionTemplate transactionTemplate) {
    return new JpaTransactionAdapter<>(transactionTemplate);
  }

  @Bean
  TransactionPort<Void> voidTransaction(TransactionTemplate transactionTemplate) {
    return new JpaTransactionAdapter<>(transactionTemplate);
  }
}
