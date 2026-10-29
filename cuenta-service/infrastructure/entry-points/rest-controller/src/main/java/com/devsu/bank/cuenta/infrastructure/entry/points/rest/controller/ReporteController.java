package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller;

import com.devsu.bank.cuenta.application.port.inbound.GenerateEstadoCuentaPort;
import com.devsu.bank.cuenta.application.query.EstadoCuentaQuery;
import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.api.ReportesApi;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated.model.EstadoCuentaResponse;
import com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.mapper.EstadoCuentaRestMapper;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * F4: {@code GET /reportes?fecha=yyyy-MM-dd,yyyy-MM-dd&cliente={clienteId}} — inclusive range in
 * the bank's time zone; a single date means that one day.
 */
@RestController
public class ReporteController implements ReportesApi {

  private final GenerateEstadoCuentaPort generateEstadoCuenta;
  private final ZoneId zona;

  public ReporteController(GenerateEstadoCuentaPort generateEstadoCuenta, ZoneId zona) {
    this.generateEstadoCuenta = generateEstadoCuenta;
    this.zona = zona;
  }

  @Override
  public ResponseEntity<EstadoCuentaResponse> generarEstadoCuenta(
      String cliente, LocalDate fechaInicio, LocalDate fechaFin, String fecha) {
    EstadoCuentaQuery query =
        new EstadoCuentaQuery(
            ClienteId.of(cliente), RangosDeFechas.requerido(fecha, fechaInicio, fechaFin));
    return ResponseEntity.ok(
        EstadoCuentaRestMapper.toResponse(generateEstadoCuenta.execute(query), zona));
  }
}
