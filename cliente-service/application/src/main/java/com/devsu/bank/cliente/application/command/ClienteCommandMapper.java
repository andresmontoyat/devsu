package com.devsu.bank.cliente.application.command;

import com.devsu.bank.cliente.domain.model.DatosPersona;
import com.devsu.bank.cliente.domain.model.Genero;
import com.devsu.bank.cliente.domain.model.Identificacion;
import com.devsu.bank.cliente.domain.model.Telefono;
import java.util.Objects;

/** Command → domain value objects. Validation happens in the value objects themselves. */
public final class ClienteCommandMapper {

  private ClienteCommandMapper() {}

  public static DatosPersona toDatosPersona(CreateClienteCommand c) {
    return datos(c.nombre(), c.genero(), c.edad(), c.identificacion(), c.direccion(), c.telefono());
  }

  public static DatosPersona toDatosPersona(UpdateClienteCommand c) {
    return datos(c.nombre(), c.genero(), c.edad(), c.identificacion(), c.direccion(), c.telefono());
  }

  public static DatosPersona merge(DatosPersona actual, PatchClienteCommand patch) {
    return new DatosPersona(
        Objects.requireNonNullElse(patch.nombre(), actual.nombre()),
        patch.genero() == null ? actual.genero() : Genero.desde(patch.genero()),
        Objects.requireNonNullElse(patch.edad(), actual.edad()),
        actual.identificacion(),
        Objects.requireNonNullElse(patch.direccion(), actual.direccion()),
        patch.telefono() == null ? actual.telefono() : new Telefono(patch.telefono()));
  }

  private static DatosPersona datos(
      String nombre,
      String genero,
      int edad,
      String identificacion,
      String direccion,
      String telefono) {
    return new DatosPersona(
        nombre,
        Genero.desde(genero),
        edad,
        new Identificacion(identificacion),
        direccion,
        new Telefono(telefono));
  }
}
