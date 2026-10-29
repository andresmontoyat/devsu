package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller;

import com.devsu.bank.cuenta.application.command.CorrectMovimientoCommand;
import com.devsu.bank.cuenta.application.command.RegisterMovimientoCommand;
import com.devsu.bank.cuenta.application.port.inbound.CorrectMovimientoPort;
import com.devsu.bank.cuenta.application.port.inbound.GetMovimientoPort;
import com.devsu.bank.cuenta.application.port.inbound.ListMovimientosPort;
import com.devsu.bank.cuenta.application.port.inbound.RegisterMovimientoPort;
import com.devsu.bank.cuenta.application.query.MovimientosQuery;
import com.devsu.bank.cuenta.application.query.PaginaSolicitud;
import com.devsu.bank.cuenta.domain.model.Movimiento;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.reporte.RangoFechas;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.api.MovimientosApi;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.model.MovimientoPage;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.model.MovimientoRequest;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.model.MovimientoResponse;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.model.MovimientoUpdateRequest;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.mapper.MovimientoRestMapper;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Implements the interface generated from openapi/cuenta-service.yaml; routes live in the contract.
 */
@RestController
public class MovimientoController implements MovimientosApi {

  private final RegisterMovimientoPort registerMovimiento;
  private final CorrectMovimientoPort correctMovimiento;
  private final GetMovimientoPort getMovimiento;
  private final ListMovimientosPort listMovimientos;

  public MovimientoController(
      RegisterMovimientoPort registerMovimiento,
      CorrectMovimientoPort correctMovimiento,
      GetMovimientoPort getMovimiento,
      ListMovimientosPort listMovimientos) {
    this.registerMovimiento = registerMovimiento;
    this.correctMovimiento = correctMovimiento;
    this.getMovimiento = getMovimiento;
    this.listMovimientos = listMovimientos;
  }

  @Override
  public ResponseEntity<MovimientoResponse> registrarMovimiento(
      MovimientoRequest request, String idempotencyKey) {
    RegisterMovimientoCommand command =
        new RegisterMovimientoCommand(
            request.getNumeroCuenta(),
            request.getValor(),
            request.getTipoMovimiento(),
            IdempotencyKeys.validar(idempotencyKey));
    Movimiento movimiento = registerMovimiento.execute(command).movimiento();
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{movimientoId}")
            .buildAndExpand(movimiento.id().value())
            .toUri();
    return ResponseEntity.created(location).body(MovimientoRestMapper.toResponse(movimiento));
  }

  @Override
  public ResponseEntity<MovimientoPage> listarMovimientos(
      String cuenta, String fecha, Integer page, Integer size) {
    MovimientosQuery query =
        new MovimientosQuery(
            cuenta == null ? null : new NumeroCuenta(cuenta),
            fecha == null ? null : RangoFechas.parse(fecha),
            new PaginaSolicitud(page, size));
    return ResponseEntity.ok(MovimientoRestMapper.toPage(listMovimientos.execute(query)));
  }

  @Override
  public ResponseEntity<MovimientoResponse> obtenerMovimiento(String movimientoId) {
    return ResponseEntity.ok(
        MovimientoRestMapper.toResponse(getMovimiento.execute(MovimientoId.of(movimientoId))));
  }

  /** Corrects the account's latest movement; any older one answers 409. */
  @Override
  public ResponseEntity<MovimientoResponse> corregirMovimiento(
      String movimientoId, MovimientoUpdateRequest request) {
    CorrectMovimientoCommand command =
        new CorrectMovimientoCommand(
            MovimientoId.of(movimientoId), request.getValor(), request.getTipoMovimiento());
    return ResponseEntity.ok(
        MovimientoRestMapper.toResponse(correctMovimiento.execute(command).movimiento()));
  }
}
