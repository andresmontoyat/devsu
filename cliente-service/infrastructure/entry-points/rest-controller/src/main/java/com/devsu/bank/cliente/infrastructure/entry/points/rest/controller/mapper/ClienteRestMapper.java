package com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.mapper;

import com.devsu.bank.cliente.application.command.CreateClienteCommand;
import com.devsu.bank.cliente.application.command.UpdateClienteCommand;
import com.devsu.bank.cliente.application.query.Pagina;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ClienteId;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.dto.ClienteRequest;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.dto.ClienteResponse;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.dto.ClienteUpdateRequest;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.dto.PageResponse;

public final class ClienteRestMapper {

  private ClienteRestMapper() {}

  public static CreateClienteCommand toCommand(ClienteRequest r) {
    return new CreateClienteCommand(
        r.nombre(),
        r.genero(),
        r.edad(),
        r.identificacion(),
        r.direccion(),
        r.telefono(),
        r.contrasena(),
        r.estado());
  }

  public static UpdateClienteCommand toCommand(ClienteId id, ClienteUpdateRequest r) {
    return new UpdateClienteCommand(
        id,
        r.nombre(),
        r.genero(),
        r.edad(),
        r.identificacion(),
        r.direccion(),
        r.telefono(),
        r.contrasena(),
        r.estado());
  }

  public static ClienteResponse toResponse(Cliente c) {
    return new ClienteResponse(
        c.getClienteId().value().toString(),
        c.getNombre(),
        c.getGenero().name(),
        c.getEdad(),
        c.getIdentificacion().value(),
        c.getDireccion(),
        c.getTelefono().value(),
        c.isActivo());
  }

  public static PageResponse<ClienteResponse> toResponse(Pagina<Cliente> pagina) {
    PageResponse.PageMetadata metadata =
        new PageResponse.PageMetadata(
            pagina.solicitud().pagina(),
            pagina.solicitud().tamano(),
            pagina.totalElementos(),
            pagina.totalPaginas());
    return new PageResponse<>(
        pagina.contenido().stream().map(ClienteRestMapper::toResponse).toList(), metadata);
  }
}
