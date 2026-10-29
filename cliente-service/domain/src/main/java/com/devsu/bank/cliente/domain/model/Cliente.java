package com.devsu.bank.cliente.domain.model;

import com.devsu.bank.cliente.domain.exception.ClienteMenorDeEdadException;
import com.devsu.bank.cliente.domain.exception.IdentificacionInmutableException;
import java.util.Objects;

/**
 * Aggregate root of the customer context: a {@link Persona} who holds a banking relationship.
 * Immutable — every state change returns a new instance and re-checks the invariants.
 */
public final class Cliente extends Persona {

  public static final int EDAD_MINIMA = 18;

  private final ClienteId clienteId;
  private final ContrasenaHash contrasena;
  private final boolean activo;
  private final long version;

  private Cliente(Builder builder) {
    super(builder.datos);
    this.clienteId = Texto.presente(builder.clienteId, "clienteId");
    this.contrasena = Texto.presente(builder.contrasena, "contrasena");
    this.activo = builder.activo;
    this.version = builder.version;
    if (getEdad() < EDAD_MINIMA) {
      throw new ClienteMenorDeEdadException(getEdad(), EDAD_MINIMA);
    }
  }

  public static Cliente registrar(ClienteId clienteId, DatosPersona datos, ContrasenaHash hash) {
    return builder().clienteId(clienteId).datos(datos).contrasena(hash).activo(true).build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public Cliente actualizarDatos(DatosPersona nuevos) {
    Texto.presente(nuevos, "datosPersona");
    if (!nuevos.identificacion().equals(getIdentificacion())) {
      throw new IdentificacionInmutableException();
    }
    return toBuilder().datos(nuevos).build();
  }

  public Cliente cambiarContrasena(ContrasenaHash nueva) {
    return toBuilder().contrasena(nueva).build();
  }

  public Cliente activar() {
    return conEstado(true);
  }

  public Cliente desactivar() {
    return conEstado(false);
  }

  public ClienteId getClienteId() {
    return clienteId;
  }

  public ContrasenaHash getContrasena() {
    return contrasena;
  }

  public boolean isActivo() {
    return activo;
  }

  /** Optimistic-lock version assigned by persistence; 0 until first stored. */
  public long getVersion() {
    return version;
  }

  private Cliente conEstado(boolean nuevoEstado) {
    return activo == nuevoEstado ? this : toBuilder().activo(nuevoEstado).build();
  }

  private Builder toBuilder() {
    return builder()
        .clienteId(clienteId)
        .datos(getDatos())
        .contrasena(contrasena)
        .activo(activo)
        .version(version);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Cliente cliente && clienteId.equals(cliente.clienteId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clienteId);
  }

  @Override
  public String toString() {
    return "Cliente[clienteId="
        + clienteId.value()
        + ", nombre="
        + getNombre()
        + ", activo="
        + activo
        + "]";
  }

  public static final class Builder {
    private ClienteId clienteId;
    private DatosPersona datos;
    private ContrasenaHash contrasena;
    private boolean activo;
    private long version;

    private Builder() {}

    public Builder clienteId(ClienteId clienteId) {
      this.clienteId = clienteId;
      return this;
    }

    public Builder datos(DatosPersona datos) {
      this.datos = datos;
      return this;
    }

    public Builder contrasena(ContrasenaHash contrasena) {
      this.contrasena = contrasena;
      return this;
    }

    public Builder activo(boolean activo) {
      this.activo = activo;
      return this;
    }

    public Builder version(long version) {
      this.version = version;
      return this;
    }

    public Cliente build() {
      return new Cliente(this);
    }
  }
}
