package com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

@SpringBootTest(classes = JpaTestApplication.class, properties = "spring.liquibase.contexts=demo")
@Import(PostgresContainerConfig.class)
class DemoSeedIT {

  @Autowired private JdbcClient jdbc;

  @Test
  void should_load_the_accounts_of_use_case_two_owned_by_the_demo_clients() {
    List<String> cuentas =
        jdbc.sql(
                "SELECT c.numero_cuenta || '|' || c.tipo_cuenta || '|' || c.saldo_disponible"
                    + " || '|' || p.nombre FROM cuenta c"
                    + " JOIN cliente_proyeccion p ON p.cliente_id = c.cliente_id ORDER BY c.id")
            .query(String.class)
            .list();

    assertThat(cuentas)
        .containsExactly(
            "478758|AHORROS|2000.00|Jose Lema",
            "225487|CORRIENTE|100.00|Marianela Montalvo",
            "495878|AHORROS|0.00|Juan Osorio",
            "496825|AHORROS|540.00|Marianela Montalvo");
  }
}
