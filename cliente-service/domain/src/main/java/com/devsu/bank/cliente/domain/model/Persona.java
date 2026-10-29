package com.devsu.bank.cliente.domain.model;

/**
 * A natural person. Sealed: the only kind of person this bounded context manages is a {@link
 * Cliente}, so the hierarchy is closed and immutable.
 */
public abstract sealed class Persona permits Cliente {

  private final DatosPersona datos;

  protected Persona(DatosPersona datos) {
    this.datos = Texto.presente(datos, "datosPersona");
  }

  public DatosPersona getDatos() {
    return datos;
  }

  public String getNombre() {
    return datos.nombre();
  }

  public Genero getGenero() {
    return datos.genero();
  }

  public int getEdad() {
    return datos.edad();
  }

  public Identificacion getIdentificacion() {
    return datos.identificacion();
  }

  public String getDireccion() {
    return datos.direccion();
  }

  public Telefono getTelefono() {
    return datos.telefono();
  }
}
