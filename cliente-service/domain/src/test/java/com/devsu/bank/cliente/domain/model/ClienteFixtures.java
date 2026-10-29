package com.devsu.bank.cliente.domain.model;

import java.util.UUID;

final class ClienteFixtures {

  static final ClienteId CLIENTE_ID =
      new ClienteId(UUID.fromString("7f1d3c9a-2b4e-4f60-9a1b-3c5d7e9f1a2b"));
  static final ContrasenaHash HASH = new ContrasenaHash("$2a$10$hashDeLaboratorioParaTests");
  static final ContrasenaHash OTRO_HASH = new ContrasenaHash("$2a$10$otroHashDeLaboratorio");
  static final int EDAD_ADULTO = 35;

  private ClienteFixtures() {}

  static DatosPersona joseLema() {
    return new DatosPersona(
        "Jose Lema",
        Genero.MASCULINO,
        EDAD_ADULTO,
        new Identificacion("1712345678"),
        "Otavalo sn y principal",
        new Telefono("098254785"));
  }

  static DatosPersona joseLemaConDireccion(String direccion) {
    DatosPersona base = joseLema();
    return new DatosPersona(
        base.nombre(),
        base.genero(),
        base.edad(),
        base.identificacion(),
        direccion,
        base.telefono());
  }

  static DatosPersona joseLemaConEdad(int edad) {
    DatosPersona base = joseLema();
    return new DatosPersona(
        base.nombre(),
        base.genero(),
        edad,
        base.identificacion(),
        base.direccion(),
        base.telefono());
  }

  static DatosPersona joseLemaConIdentificacion(String identificacion) {
    DatosPersona base = joseLema();
    return new DatosPersona(
        base.nombre(),
        base.genero(),
        base.edad(),
        new Identificacion(identificacion),
        base.direccion(),
        base.telefono());
  }

  static Cliente clienteRegistrado() {
    return Cliente.registrar(CLIENTE_ID, joseLema(), HASH);
  }
}
