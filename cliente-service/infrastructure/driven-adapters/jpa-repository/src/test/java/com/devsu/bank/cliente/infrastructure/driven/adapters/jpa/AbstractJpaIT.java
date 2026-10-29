package com.devsu.bank.cliente.infrastructure.driven.adapters.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(classes = JpaTestApplication.class)
@Import(PostgresContainerConfig.class)
abstract class AbstractJpaIT {

  @Autowired protected JdbcClient jdbc;
  @Autowired protected TransactionTemplate transactionTemplate;

  @BeforeEach
  void limpiarTablas() {
    jdbc.sql("TRUNCATE outbox_message, cliente, persona RESTART IDENTITY CASCADE").update();
  }

  protected <T> T enTransaccion(java.util.function.Supplier<T> accion) {
    return transactionTemplate.execute(status -> accion.get());
  }
}
