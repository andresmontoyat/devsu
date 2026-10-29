package com.devsu.bank.cliente.infrastructure.driven.adapters.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Root of the JOINED hierarchy: one {@code persona} row per person, plus one row in the subtype
 * table. Hibernate requires the {@code @Version} attribute on the root entity.
 */
@Entity
@Table(name = "persona")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class PersonaJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String nombre;

  @Column(nullable = false, length = 10)
  private String genero;

  @Column(nullable = false)
  private short edad;

  @Column(nullable = false, length = 20, unique = true)
  private String identificacion;

  @Column(nullable = false, length = 200)
  private String direccion;

  @Column(nullable = false, length = 16)
  private String telefono;

  @Version private long version;

  public Long getId() {
    return id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getGenero() {
    return genero;
  }

  public void setGenero(String genero) {
    this.genero = genero;
  }

  public short getEdad() {
    return edad;
  }

  public void setEdad(short edad) {
    this.edad = edad;
  }

  public String getIdentificacion() {
    return identificacion;
  }

  public void setIdentificacion(String identificacion) {
    this.identificacion = identificacion;
  }

  public String getDireccion() {
    return direccion;
  }

  public void setDireccion(String direccion) {
    this.direccion = direccion;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  public long getVersion() {
    return version;
  }
}
