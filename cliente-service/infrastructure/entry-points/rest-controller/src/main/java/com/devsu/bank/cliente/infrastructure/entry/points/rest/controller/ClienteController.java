package com.devsu.bank.cliente.infrastructure.entry.points.rest.controller;

import com.devsu.bank.cliente.application.port.inbound.CreateClientePort;
import com.devsu.bank.cliente.application.port.inbound.DeleteClientePort;
import com.devsu.bank.cliente.application.port.inbound.GetClientePort;
import com.devsu.bank.cliente.application.port.inbound.ListClientesPort;
import com.devsu.bank.cliente.application.port.inbound.PatchClientePort;
import com.devsu.bank.cliente.application.port.inbound.UpdateClientePort;
import com.devsu.bank.cliente.application.query.PaginaSolicitud;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ClienteId;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.generated.api.ClientesApi;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.generated.model.ClientePage;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.generated.model.ClienteRequest;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.generated.model.ClienteResponse;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.generated.model.ClienteUpdateRequest;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.mapper.ClientePatchMapper;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.mapper.ClienteRestMapper;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tools.jackson.databind.JsonNode;

/**
 * Implements the interface generated from openapi/cliente-service.yaml; routes live in the
 * contract.
 */
@RestController
public class ClienteController implements ClientesApi {

  private final CreateClientePort createCliente;
  private final GetClientePort getCliente;
  private final ListClientesPort listClientes;
  private final UpdateClientePort updateCliente;
  private final PatchClientePort patchCliente;
  private final DeleteClientePort deleteCliente;

  public ClienteController(
      CreateClientePort createCliente,
      GetClientePort getCliente,
      ListClientesPort listClientes,
      UpdateClientePort updateCliente,
      PatchClientePort patchCliente,
      DeleteClientePort deleteCliente) {
    this.createCliente = createCliente;
    this.getCliente = getCliente;
    this.listClientes = listClientes;
    this.updateCliente = updateCliente;
    this.patchCliente = patchCliente;
    this.deleteCliente = deleteCliente;
  }

  @Override
  public ResponseEntity<ClienteResponse> crearCliente(ClienteRequest request) {
    Cliente creado = createCliente.execute(ClienteRestMapper.toCommand(request));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{clienteId}")
            .buildAndExpand(creado.getClienteId().value())
            .toUri();
    return ResponseEntity.created(location).body(ClienteRestMapper.toResponse(creado));
  }

  @Override
  public ResponseEntity<ClientePage> listarClientes(Integer page, Integer size) {
    return ResponseEntity.ok(
        ClienteRestMapper.toResponse(listClientes.execute(new PaginaSolicitud(page, size))));
  }

  @Override
  public ResponseEntity<ClienteResponse> obtenerCliente(String clienteId) {
    return ResponseEntity.ok(
        ClienteRestMapper.toResponse(getCliente.execute(ClienteId.of(clienteId))));
  }

  @Override
  public ResponseEntity<ClienteResponse> reemplazarCliente(
      String clienteId, ClienteUpdateRequest request) {
    Cliente actualizado =
        updateCliente.execute(ClienteRestMapper.toCommand(ClienteId.of(clienteId), request));
    return ResponseEntity.ok(ClienteRestMapper.toResponse(actualizado));
  }

  @Override
  public ResponseEntity<ClienteResponse> modificarCliente(String clienteId, JsonNode patch) {
    Cliente actualizado =
        patchCliente.execute(ClientePatchMapper.toCommand(ClienteId.of(clienteId), patch));
    return ResponseEntity.ok(ClienteRestMapper.toResponse(actualizado));
  }

  @Override
  public ResponseEntity<Void> eliminarCliente(String clienteId) {
    deleteCliente.execute(ClienteId.of(clienteId));
    return ResponseEntity.noContent().build();
  }
}
