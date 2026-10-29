package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller;

import com.devsu.bank.cuenta.application.port.inbound.CreateCuentaPort;
import com.devsu.bank.cuenta.application.port.inbound.GetCuentaPort;
import com.devsu.bank.cuenta.application.port.inbound.ListCuentasPort;
import com.devsu.bank.cuenta.application.port.inbound.PatchCuentaPort;
import com.devsu.bank.cuenta.application.port.inbound.UpdateCuentaPort;
import com.devsu.bank.cuenta.application.query.CuentasQuery;
import com.devsu.bank.cuenta.application.query.PaginaSolicitud;
import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto.CuentaRequest;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto.CuentaResponse;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto.CuentaUpdateRequest;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto.PageResponse;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.mapper.CuentaPatchMapper;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.mapper.CuentaRestMapper;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
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

/** F1 CRU of accounts. No DELETE: an account with history is deactivated (PATCH estado). */
@RestController
@RequestMapping("/cuentas")
public class CuentaController {

  private static final String MERGE_PATCH = "application/merge-patch+json";

  private final CreateCuentaPort createCuenta;
  private final GetCuentaPort getCuenta;
  private final ListCuentasPort listCuentas;
  private final UpdateCuentaPort updateCuenta;
  private final PatchCuentaPort patchCuenta;

  public CuentaController(
      CreateCuentaPort createCuenta,
      GetCuentaPort getCuenta,
      ListCuentasPort listCuentas,
      UpdateCuentaPort updateCuenta,
      PatchCuentaPort patchCuenta) {
    this.createCuenta = createCuenta;
    this.getCuenta = getCuenta;
    this.listCuentas = listCuentas;
    this.updateCuenta = updateCuenta;
    this.patchCuenta = patchCuenta;
  }

  @PostMapping
  public ResponseEntity<CuentaResponse> abrir(@Valid @RequestBody CuentaRequest request) {
    Cuenta cuenta = createCuenta.execute(CuentaRestMapper.toCommand(request));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{numeroCuenta}")
            .buildAndExpand(cuenta.getNumeroCuenta().value())
            .toUri();
    return ResponseEntity.created(location).body(CuentaRestMapper.toResponse(cuenta));
  }

  @GetMapping
  public PageResponse<CuentaResponse> listar(
      @RequestParam(required = false) String cliente,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    ClienteId clienteId = cliente == null ? null : ClienteId.of(cliente);
    return CuentaRestMapper.toPage(
        listCuentas.execute(new CuentasQuery(clienteId, new PaginaSolicitud(page, size))),
        CuentaRestMapper::toResponse);
  }

  @GetMapping("/{numeroCuenta}")
  public CuentaResponse obtener(@PathVariable String numeroCuenta) {
    return CuentaRestMapper.toResponse(getCuenta.execute(new NumeroCuenta(numeroCuenta)));
  }

  @PutMapping("/{numeroCuenta}")
  public CuentaResponse reemplazar(
      @PathVariable String numeroCuenta, @Valid @RequestBody CuentaUpdateRequest request) {
    return CuentaRestMapper.toResponse(
        updateCuenta.execute(CuentaRestMapper.toCommand(numeroCuenta, request)));
  }

  @PatchMapping(
      path = "/{numeroCuenta}",
      consumes = {MERGE_PATCH, "application/json"})
  public CuentaResponse modificar(@PathVariable String numeroCuenta, @RequestBody JsonNode patch) {
    return CuentaRestMapper.toResponse(
        patchCuenta.execute(CuentaPatchMapper.toCommand(numeroCuenta, patch)));
  }
}
