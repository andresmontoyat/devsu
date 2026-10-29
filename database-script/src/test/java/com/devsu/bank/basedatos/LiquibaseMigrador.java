package com.devsu.bank.basedatos;

import java.sql.Connection;
import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.DirectoryResourceAccessor;

/** Applies a service changelog exactly as the service does at startup, with the demo context. */
final class LiquibaseMigrador {

  private static final String CONTEXTO = "demo";

  private LiquibaseMigrador() {}

  static void migrar(Connection conexion, ServicioBaseDatos servicio) throws Exception {
    Database database =
        DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(new JdbcConnection(conexion));
    try (var recursos = new DirectoryResourceAccessor(servicio.changelogRoot())) {
      Scope.child(Scope.Attr.resourceAccessor.name(), recursos, () -> actualizar(database));
    }
  }

  private static void actualizar(Database database) throws Exception {
    new CommandScope(UpdateCommandStep.COMMAND_NAME)
        .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database)
        .addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, ServicioBaseDatos.CHANGELOG)
        .addArgumentValue(UpdateCommandStep.CONTEXTS_ARG, CONTEXTO)
        .execute();
  }
}
