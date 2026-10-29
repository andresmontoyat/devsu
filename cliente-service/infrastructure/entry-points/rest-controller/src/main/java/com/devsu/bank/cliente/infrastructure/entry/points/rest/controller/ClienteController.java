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
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.dto.ClienteRequest;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.dto.ClienteResponse;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.dto.ClienteUpdateRequest;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.dto.PageResponse;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.mapper.ClientePatchMapper;
import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.mapper.ClienteRestMapper;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

  private static final String MERGE_PATCH = "application/merge-patch+json";

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

  @PostMapping
  public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
    Cliente creado = createCliente.execute(ClienteRestMapper.toCommand(request));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{clienteId}")
            .buildAndExpand(creado.getClienteId().value())
            .toUri();
    return ResponseEntity.created(location).body(ClienteRestMapper.toResponse(creado));
  }

  @GetMapping
  public PageResponse<ClienteResponse> listar(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ClienteRestMapper.toResponse(listClientes.execute(new PaginaSolicitud(page, size)));
  }

  @GetMapping("/{clienteId}")
  public ClienteResponse obtener(@PathVariable String clienteId) {
    return ClienteRestMapper.toResponse(getCliente.execute(ClienteId.of(clienteId)));
  }

  @PutMapping("/{clienteId}")
  public ClienteResponse reemplazar(
      @PathVariable String clienteId, @Valid @RequestBody ClienteUpdateRequest request) {
    Cliente actualizado =
        updateCliente.execute(ClienteRestMapper.toCommand(ClienteId.of(clienteId), request));
    return ClienteRestMapper.toResponse(actualizado);
  }

  @PatchMapping(
      path = "/{clienteId}",
      consumes = {MERGE_PATCH, "application/json"})
  public ClienteResponse modificar(@PathVariable String clienteId, @RequestBody JsonNode patch) {
    Cliente actualizado =
        patchCliente.execute(ClientePatchMapper.toCommand(ClienteId.of(clienteId), patch));
    return ClienteRestMapper.toResponse(actualizado);
  }

  @DeleteMapping("/{clienteId}")
  public ResponseEntity<Void> eliminar(@PathVariable String clienteId) {
    deleteCliente.execute(ClienteId.of(clienteId));
    return ResponseEntity.noContent().build();
  }
}
