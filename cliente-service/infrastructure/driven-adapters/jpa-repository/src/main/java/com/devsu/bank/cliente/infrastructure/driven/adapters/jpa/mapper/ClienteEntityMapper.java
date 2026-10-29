package com.devsu.bank.cliente.infrastructure.driven.adapters.jpa.mapper;

import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ClienteId;
import com.devsu.bank.cliente.domain.model.ContrasenaHash;
import com.devsu.bank.cliente.domain.model.DatosPersona;
import com.devsu.bank.cliente.domain.model.Genero;
import com.devsu.bank.cliente.domain.model.Identificacion;
import com.devsu.bank.cliente.domain.model.Telefono;
import com.devsu.bank.cliente.infrastructure.driven.adapters.jpa.entity.ClienteJpaEntity;

/** Domain ↔ JPA entity. The entity never leaves this module. */
public final class ClienteEntityMapper {

  private ClienteEntityMapper() {}

  public static Cliente toDomain(ClienteJpaEntity entity) {
    DatosPersona datos =
        new DatosPersona(
            entity.getNombre(),
            Genero.valueOf(entity.getGenero()),
            entity.getEdad(),
            new Identificacion(entity.getIdentificacion()),
            entity.getDireccion(),
            new Telefono(entity.getTelefono()));
    return Cliente.builder()
        .clienteId(new ClienteId(entity.getClienteId()))
        .datos(datos)
        .contrasena(new ContrasenaHash(entity.getContrasenaHash()))
        .activo(entity.isEstado())
        .version(entity.getVersion())
        .build();
  }

  public static ClienteJpaEntity toNewEntity(Cliente cliente) {
    ClienteJpaEntity entity = new ClienteJpaEntity();
    entity.setClienteId(cliente.getClienteId().value());
    return copyInto(cliente, entity);
  }

  public static ClienteJpaEntity copyInto(Cliente cliente, ClienteJpaEntity entity) {
    entity.setNombre(cliente.getNombre());
    entity.setGenero(cliente.getGenero().name());
    entity.setEdad((short) cliente.getEdad());
    entity.setIdentificacion(cliente.getIdentificacion().value());
    entity.setDireccion(cliente.getDireccion());
    entity.setTelefono(cliente.getTelefono().value());
    entity.setContrasenaHash(cliente.getContrasena().value());
    entity.setEstado(cliente.isActivo());
    return entity;
  }
}
