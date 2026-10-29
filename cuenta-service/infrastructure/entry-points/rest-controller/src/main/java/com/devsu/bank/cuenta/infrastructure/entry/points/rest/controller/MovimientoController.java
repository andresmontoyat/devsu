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
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto.MovimientoRequest;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto.MovimientoResponse;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto.MovimientoUpdateRequest;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto.PageResponse;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.mapper.CuentaRestMapper;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.mapper.MovimientoRestMapper;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** F1 CRU + F2/F3 of movements. No DELETE: the ledger is append-only except for its tail. */
@RestController
@RequestMapping("/movimientos")
public class MovimientoController {

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

  @PostMapping
  public ResponseEntity<MovimientoResponse> registrar(
      @Valid @RequestBody MovimientoRequest request,
      @RequestHeader(name = IdempotencyKeys.HEADER, required = false) String idempotencyKey) {
    RegisterMovimientoCommand command =
        new RegisterMovimientoCommand(
            request.numeroCuenta(),
            request.valor(),
            request.tipoMovimiento(),
            IdempotencyKeys.validar(idempotencyKey));
    Movimiento movimiento = registerMovimiento.execute(command).movimiento();
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{movimientoId}")
            .buildAndExpand(movimiento.id().value())
            .toUri();
    return ResponseEntity.created(location).body(MovimientoRestMapper.toResponse(movimiento));
  }

  @GetMapping
  public PageResponse<MovimientoResponse> listar(
      @RequestParam(required = false) String cuenta,
      @RequestParam(required = false) String fecha,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    MovimientosQuery query =
        new MovimientosQuery(
            cuenta == null ? null : new NumeroCuenta(cuenta),
            fecha == null ? null : RangoFechas.parse(fecha),
            new PaginaSolicitud(page, size));
    return CuentaRestMapper.toPage(
        listMovimientos.execute(query), MovimientoRestMapper::toResponse);
  }

  @GetMapping("/{movimientoId}")
  public MovimientoResponse obtener(@PathVariable String movimientoId) {
    return MovimientoRestMapper.toResponse(getMovimiento.execute(MovimientoId.of(movimientoId)));
  }

  /** Corrects the account's latest movement; any older one answers 409. */
  @PutMapping("/{movimientoId}")
  public MovimientoResponse corregir(
      @PathVariable String movimientoId, @Valid @RequestBody MovimientoUpdateRequest request) {
    CorrectMovimientoCommand command =
        new CorrectMovimientoCommand(
            MovimientoId.of(movimientoId), request.valor(), request.tipoMovimiento());
    return MovimientoRestMapper.toResponse(correctMovimiento.execute(command).movimiento());
  }
}
