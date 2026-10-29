package com.devsu.bank.basedatos;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/** Throwaway Postgres, same version as docker compose, driven through its own psql and pg_dump. */
final class PostgresServidor implements AutoCloseable {

  private static final DockerImageName IMAGEN = DockerImageName.parse("postgres:17.11-alpine");
  private static final String SCRIPT_EN_CONTENEDOR = "/tmp/BaseDatos.sql";

  private final PostgreSQLContainer container;

  private PostgresServidor(PostgreSQLContainer container) {
    this.container = container;
  }

  static PostgresServidor iniciar() {
    PostgreSQLContainer container = new PostgreSQLContainer(IMAGEN);
    container.start();
    return new PostgresServidor(container);
  }

  void crearBaseDatos(String nombre) {
    psql("postgres", "CREATE DATABASE " + nombre);
  }

  Connection conectar(String database) throws SQLException {
    String url =
        "jdbc:postgresql://%s:%d/%s"
            .formatted(container.getHost(), container.getFirstMappedPort(), database);
    return DriverManager.getConnection(url, container.getUsername(), container.getPassword());
  }

  /** Schema and rows only: no roles, grants or Liquibase bookkeeping, so any server can load it. */
  String volcar(String database) {
    return ejecutar(
        "pg_dump",
        "-U",
        container.getUsername(),
        "-d",
        database,
        "--no-owner",
        "--no-privileges",
        "--column-inserts",
        "--exclude-table=public.databasechangelog",
        "--exclude-table=public.databasechangeloglock");
  }

  void aplicarScript(Path script) {
    container.copyFileToContainer(MountableFile.forHostPath(script), SCRIPT_EN_CONTENEDOR);
    ejecutar(
        "psql",
        "-v",
        "ON_ERROR_STOP=1",
        "-U",
        container.getUsername(),
        "-d",
        "postgres",
        "-f",
        SCRIPT_EN_CONTENEDOR);
  }

  String psql(String database, String sql) {
    return ejecutar(
            "psql",
            "-v",
            "ON_ERROR_STOP=1",
            "-U",
            container.getUsername(),
            "-d",
            database,
            "-tAc",
            sql)
        .strip();
  }

  private String ejecutar(String... comando) {
    try {
      ExecResult resultado = container.execInContainer(comando);
      if (resultado.getExitCode() != 0) {
        throw new IllegalStateException(comando[0] + " failed: " + resultado.getStderr());
      }
      return resultado.getStdout();
    } catch (IOException e) {
      throw new IllegalStateException(comando[0] + " could not run", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(comando[0] + " was interrupted", e);
    }
  }

  @Override
  public void close() {
    container.stop();
  }
}
