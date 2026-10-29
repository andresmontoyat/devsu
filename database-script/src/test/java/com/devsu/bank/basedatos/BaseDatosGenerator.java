package com.devsu.bank.basedatos;

import java.nio.file.Files;

/** Entry point of {@code ./gradlew generateBaseDatos}. */
public final class BaseDatosGenerator {

  private BaseDatosGenerator() {}

  public static void main(String[] args) throws Exception {
    Files.writeString(ServicioBaseDatos.script(), BaseDatosScript.generar());
  }
}
