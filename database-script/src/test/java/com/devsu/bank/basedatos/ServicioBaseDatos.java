package com.devsu.bank.basedatos;

import java.nio.file.Path;
import java.util.List;

/** A service database and the resources root that holds its Liquibase changelog. */
record ServicioBaseDatos(String database, String servicio, Path changelogRoot) {

  static final String CHANGELOG = "changelog/db.changelog-master.yaml";

  static List<ServicioBaseDatos> todos() {
    return List.of(
        new ServicioBaseDatos("cliente_db", "cliente-service", ruta("basedatos.changelog.cliente")),
        new ServicioBaseDatos("cuenta_db", "cuenta-service", ruta("basedatos.changelog.cuenta")));
  }

  static Path script() {
    return ruta("basedatos.script");
  }

  private static Path ruta(String propiedad) {
    String valor = System.getProperty(propiedad);
    if (valor == null) {
      throw new IllegalStateException(
          "Missing system property " + propiedad + "; run through Gradle");
    }
    return Path.of(valor);
  }
}
