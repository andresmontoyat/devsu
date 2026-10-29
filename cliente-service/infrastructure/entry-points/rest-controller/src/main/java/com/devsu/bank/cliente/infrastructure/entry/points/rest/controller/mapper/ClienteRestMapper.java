package com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.mapper;

import com.devsu.bank.cliente.application.command.CreateClienteCommand;
import com.devsu.bank.cliente.application.command.UpdateClienteCommand;
import com.devsu.bank.cliente.application.query.Pagina;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ClienteId;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.generated.model.ClientePage;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.generated.model.ClienteRequest;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.generated.model.ClienteResponse;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.generated.model.ClienteUpdateRequest;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.generated.model.Genero;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.generated.model.PageMetadata;

/** Maps the models generated from the OpenAPI contract to commands and back. */
public final class ClienteRestMapper {

  private ClienteRestMapper() {}

  public static CreateClienteCommand toCommand(ClienteRequest r) {
    return new CreateClienteCommand(
        r.getNombre(),
        r.getGenero(),
        r.getEdad(),
        r.getIdentificacion(),
        r.getDireccion(),
        r.getTelefono(),
        r.getContrasena(),
        r.getEstado());
  }

  public static UpdateClienteCommand toCommand(ClienteId id, ClienteUpdateRequest r) {
    return new UpdateClienteCommand(
        id,
        r.getNombre(),
        r.getGenero(),
        r.getEdad(),
        r.getIdentificacion(),
        r.getDireccion(),
        r.getTelefono(),
        r.getContrasena(),
        r.getEstado());
  }

  public static ClienteResponse toResponse(Cliente c) {
    return new ClienteResponse(
        c.getClienteId().value(),
        c.getNombre(),
        Genero.fromValue(c.getGenero().name()),
        c.getEdad(),
        c.getIdentificacion().value(),
        c.getDireccion(),
        c.getTelefono().value(),
        c.isActivo());
  }

  public static ClientePage toResponse(Pagina<Cliente> pagina) {
    PageMetadata metadata =
        new PageMetadata(
            pagina.solicitud().pagina(),
            pagina.solicitud().tamano(),
            pagina.totalElementos(),
            (long) pagina.totalPaginas());
    return new ClientePage(
        pagina.contenido().stream().map(ClienteRestMapper::toResponse).toList(), metadata);
  }
}
