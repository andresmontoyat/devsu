package com.devsu.bank.cliente.infrastructure.driven.adapters.jpa;

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
  void should_load_the_three_customers_of_the_exercise_with_fixed_ids() {
    List<String> clientes =
        jdbc.sql(
                "SELECT p.nombre || '|' || c.cliente_id || '|' || c.estado FROM cliente c"
                    + " JOIN persona p ON p.id = c.persona_id ORDER BY p.id")
            .query(String.class)
            .list();

    assertThat(clientes)
        .containsExactly(
            "Jose Lema|0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e01|true",
            "Marianela Montalvo|0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e02|true",
            "Juan Osorio|0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e03|true");
  }
}
