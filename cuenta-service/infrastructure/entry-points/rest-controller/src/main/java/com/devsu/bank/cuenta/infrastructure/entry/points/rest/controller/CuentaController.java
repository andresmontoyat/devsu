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
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.api.CuentasApi;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.model.CuentaPage;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.model.CuentaRequest;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.model.CuentaResponse;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.model.CuentaUpdateRequest;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.mapper.CuentaPatchMapper;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.mapper.CuentaRestMapper;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tools.jackson.databind.JsonNode;

/**
 * Implements the interface generated from openapi/cuenta-service.yaml; routes live in the contract.
 */
@RestController
public class CuentaController implements CuentasApi {

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

  @Override
  public ResponseEntity<CuentaResponse> abrirCuenta(CuentaRequest request) {
    Cuenta cuenta = createCuenta.execute(CuentaRestMapper.toCommand(request));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{numeroCuenta}")
            .buildAndExpand(cuenta.getNumeroCuenta().value())
            .toUri();
    return ResponseEntity.created(location).body(CuentaRestMapper.toResponse(cuenta));
  }

  @Override
  public ResponseEntity<CuentaPage> listarCuentas(String cliente, Integer page, Integer size) {
    ClienteId clienteId = cliente == null ? null : ClienteId.of(cliente);
    CuentasQuery query = new CuentasQuery(clienteId, new PaginaSolicitud(page, size));
    return ResponseEntity.ok(CuentaRestMapper.toPage(listCuentas.execute(query)));
  }

  @Override
  public ResponseEntity<CuentaResponse> obtenerCuenta(String numeroCuenta) {
    return ResponseEntity.ok(
        CuentaRestMapper.toResponse(getCuenta.execute(new NumeroCuenta(numeroCuenta))));
  }

  @Override
  public ResponseEntity<CuentaResponse> reemplazarCuenta(
      String numeroCuenta, CuentaUpdateRequest request) {
    return ResponseEntity.ok(
        CuentaRestMapper.toResponse(
            updateCuenta.execute(CuentaRestMapper.toCommand(numeroCuenta, request))));
  }

  @Override
  public ResponseEntity<CuentaResponse> modificarCuenta(String numeroCuenta, JsonNode patch) {
    return ResponseEntity.ok(
        CuentaRestMapper.toResponse(
            patchCuenta.execute(CuentaPatchMapper.toCommand(numeroCuenta, patch))));
  }
}
