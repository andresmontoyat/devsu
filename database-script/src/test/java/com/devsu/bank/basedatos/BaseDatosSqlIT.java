package com.devsu.bank.basedatos;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class BaseDatosSqlIT {

  private static final Path SCRIPT = ServicioBaseDatos.script();

  @Test
  void should_match_the_liquibase_changelogs_when_regenerated() throws Exception {
    assertThat(Files.readString(SCRIPT))
        .as("BaseDatos.sql is out of date with the changelogs: run ./gradlew generateBaseDatos")
        .isEqualTo(BaseDatosScript.generar());
  }

  @Test
  void should_create_both_databases_with_the_demo_data_when_applied_to_an_empty_server() {
    try (PostgresServidor servidor = PostgresServidor.iniciar()) {
      servidor.aplicarScript(SCRIPT);

      assertThat(
              servidor.psql(
                  "cliente_db",
                  "SELECT string_agg(p.nombre, ',' ORDER BY p.nombre)"
                      + " FROM cliente c JOIN persona p ON p.id = c.persona_id"))
          .isEqualTo("Jose Lema,Juan Osorio,Marianela Montalvo");
      assertThat(
              servidor.psql(
                  "cuenta_db",
                  "SELECT string_agg(numero_cuenta, ',' ORDER BY numero_cuenta) FROM cuenta"))
          .isEqualTo("225487,478758,495878,496825");
    }
  }
}
