package com.devsu.bank.cliente.domain.model;

import com.devsu.bank.cliente.domain.exception.DatoInvalidoException;

/** The personal attributes every {@link Persona} carries, validated as one unit. */
public record DatosPersona(
    String nombre,
    Genero genero,
    int edad,
    Identificacion identificacion,
    String direccion,
    Telefono telefono) {

  public static final int NOMBRE_MAX = 100;
  public static final int DIRECCION_MAX = 200;
  public static final int EDAD_MAX = 150;

  public DatosPersona {
    nombre = Texto.requerido(nombre, "nombre", NOMBRE_MAX);
    direccion = Texto.requerido(direccion, "direccion", DIRECCION_MAX);
    Texto.presente(genero, "genero");
    Texto.presente(identificacion, "identificacion");
    Texto.presente(telefono, "telefono");
    if (edad < 0 || edad > EDAD_MAX) {
      throw new DatoInvalidoException("edad", "debe estar entre 0 y " + EDAD_MAX);
    }
  }
}
