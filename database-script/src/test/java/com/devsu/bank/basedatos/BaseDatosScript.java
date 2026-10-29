package com.devsu.bank.basedatos;

import java.sql.Connection;
import java.util.stream.Collectors;

/** Builds BaseDatos.sql: both service databases, migrated by Liquibase and dumped with pg_dump. */
final class BaseDatosScript {

  private static final String CABECERA =
      """
      -- BaseDatos.sql: esquema y datos de ejemplo de devsu-bank (cliente_db y cuenta_db).
      --
      -- ARCHIVO GENERADO desde los changelogs de Liquibase, que son la fuente de verdad del esquema.
      -- No editar a mano: ./gradlew generateBaseDatos lo regenera y BaseDatosSqlIT falla si difiere.
      --
      -- Uso:       psql -U postgres -f BaseDatos.sql   (crea ambas bases; requiere psql por \\connect)
      -- Servicios: no lo usan; cada uno aplica sus migraciones con Liquibase al arrancar.
      """;

  private BaseDatosScript() {}

  static String generar() throws Exception {
    try (PostgresServidor servidor = PostgresServidor.iniciar()) {
      StringBuilder script = new StringBuilder(CABECERA);
      for (ServicioBaseDatos servicio : ServicioBaseDatos.todos()) {
        script.append(seccion(servidor, servicio));
      }
      return script.toString();
    }
  }

  private static String seccion(PostgresServidor servidor, ServicioBaseDatos servicio)
      throws Exception {
    servidor.crearBaseDatos(servicio.database());
    try (Connection conexion = servidor.conectar(servicio.database())) {
      LiquibaseMigrador.migrar(conexion, servicio);
    }
    return """

        -- =============================================================================
        -- %s (%s)
        -- =============================================================================
        CREATE DATABASE %s;
        \\connect %s
        %s"""
        .formatted(
            servicio.database(),
            servicio.servicio(),
            servicio.database(),
            servicio.database(),
            normalizar(servidor.volcar(servicio.database())));
  }

  /** pg_dump 17.6+ wraps the dump in \restrict with a random key; dropped to keep output stable. */
  static String normalizar(String volcado) {
    return volcado
        .lines()
        .filter(linea -> !linea.startsWith("\\restrict") && !linea.startsWith("\\unrestrict"))
        .collect(Collectors.joining("\n", "", "\n"));
  }
}
