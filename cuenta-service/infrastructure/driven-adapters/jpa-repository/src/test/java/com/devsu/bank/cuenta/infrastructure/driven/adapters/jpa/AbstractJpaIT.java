package com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa;

import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.model.ClienteProyeccion;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.model.TipoCuenta;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.adapter.ClienteProyeccionRepositoryAdapter;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.adapter.CuentaRepositoryAdapter;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(classes = JpaTestApplication.class)
@Import(PostgresContainerConfig.class)
abstract class AbstractJpaIT {

  protected static final ClienteId MARIANELA = ClienteId.of("0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e02");

  @Autowired protected JdbcClient jdbc;
  @Autowired protected TransactionTemplate transactionTemplate;
  @Autowired protected CuentaRepositoryAdapter cuentas;
  @Autowired protected ClienteProyeccionRepositoryAdapter clientes;

  @BeforeEach
  void limpiarTablas() {
    jdbc.sql(
            "TRUNCATE movimiento, cuenta, cliente_proyeccion, consumer_processed_event"
                + " RESTART IDENTITY CASCADE")
        .update();
    enTransaccion(
        () -> clientes.save(ClienteProyeccion.desde(MARIANELA, "Marianela Montalvo", true, 0)));
  }

  protected <T> T enTransaccion(Supplier<T> accion) {
    return transactionTemplate.execute(status -> accion.get());
  }

  protected Cuenta abrir(String numero, String saldo) {
    return enTransaccion(
        () ->
            cuentas.insert(
                Cuenta.abrir(
                    new NumeroCuenta(numero), TipoCuenta.AHORROS, Monto.of(saldo), MARIANELA)));
  }
}
