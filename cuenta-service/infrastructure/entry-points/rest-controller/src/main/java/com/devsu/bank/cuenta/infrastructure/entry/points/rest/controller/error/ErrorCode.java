package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.error;

import com.devsu.bank.cuenta.domain.exception.ClienteNoDisponibleException;
import com.devsu.bank.cuenta.domain.exception.ClienteNoEncontradoException;
import com.devsu.bank.cuenta.domain.exception.CuentaInactivaException;
import com.devsu.bank.cuenta.domain.exception.CuentaModificadaConcurrentementeException;
import com.devsu.bank.cuenta.domain.exception.CuentaNoEncontradaException;
import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import com.devsu.bank.cuenta.domain.exception.DomainException;
import com.devsu.bank.cuenta.domain.exception.MovimientoNoEncontradoException;
import com.devsu.bank.cuenta.domain.exception.MovimientoNoModificableException;
import com.devsu.bank.cuenta.domain.exception.NumeroCuentaDuplicadoException;
import com.devsu.bank.cuenta.domain.exception.RangoFechasInvalidoException;
import com.devsu.bank.cuenta.domain.exception.SaldoInicialNoModificableException;
import com.devsu.bank.cuenta.domain.exception.SaldoNoDisponibleException;
import com.devsu.bank.cuenta.domain.exception.ValorMovimientoInvalidoException;
import java.net.URI;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/** Stable, machine-readable error contract: clients branch on {@code code}, never on text. */
public enum ErrorCode {
  DATO_INVALIDO(HttpStatus.BAD_REQUEST, "Dato invalido"),
  VALIDACION_FALLIDA(HttpStatus.BAD_REQUEST, "Validacion fallida"),
  CUERPO_INVALIDO(HttpStatus.BAD_REQUEST, "Cuerpo de la peticion invalido"),
  PARAMETRO_INVALIDO(HttpStatus.BAD_REQUEST, "Parametro invalido"),
  RANGO_FECHAS_INVALIDO(HttpStatus.BAD_REQUEST, "Rango de fechas invalido"),
  CUENTA_NO_ENCONTRADA(HttpStatus.NOT_FOUND, "Cuenta no encontrada"),
  MOVIMIENTO_NO_ENCONTRADO(HttpStatus.NOT_FOUND, "Movimiento no encontrado"),
  CLIENTE_NO_ENCONTRADO(HttpStatus.NOT_FOUND, "Cliente no encontrado"),
  RECURSO_NO_ENCONTRADO(HttpStatus.NOT_FOUND, "Recurso no encontrado"),
  METODO_NO_PERMITIDO(HttpStatus.METHOD_NOT_ALLOWED, "Metodo no permitido"),
  TIPO_DE_CONTENIDO_NO_SOPORTADO(
      HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de contenido no soportado"),
  NUMERO_CUENTA_DUPLICADO(HttpStatus.CONFLICT, "Numero de cuenta duplicado"),
  CONFLICTO_DE_CONCURRENCIA(HttpStatus.CONFLICT, "Conflicto de concurrencia"),
  MOVIMIENTO_NO_MODIFICABLE(HttpStatus.CONFLICT, "Movimiento no modificable"),
  SALDO_INICIAL_NO_MODIFICABLE(HttpStatus.CONFLICT, "Saldo inicial no modificable"),
  IDEMPOTENCY_KEY_EN_CURSO(HttpStatus.CONFLICT, "Peticion en curso"),
  SALDO_NO_DISPONIBLE(HttpStatus.UNPROCESSABLE_CONTENT, SaldoNoDisponibleException.MENSAJE),
  CUENTA_INACTIVA(HttpStatus.UNPROCESSABLE_CONTENT, "Cuenta inactiva"),
  CLIENTE_NO_DISPONIBLE(HttpStatus.UNPROCESSABLE_CONTENT, "Cliente no disponible"),
  VALOR_MOVIMIENTO_INVALIDO(HttpStatus.UNPROCESSABLE_CONTENT, "Valor de movimiento invalido"),
  IDEMPOTENCY_KEY_REUTILIZADA(HttpStatus.UNPROCESSABLE_CONTENT, "Idempotency-Key reutilizada"),
  REGLA_DE_NEGOCIO(HttpStatus.UNPROCESSABLE_CONTENT, "Regla de negocio incumplida"),
  IDEMPOTENCY_NO_DISPONIBLE(HttpStatus.SERVICE_UNAVAILABLE, "Servicio temporalmente no disponible"),
  ERROR_HTTP(HttpStatus.BAD_REQUEST, "Peticion no procesable"),
  ERROR_INTERNO(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno");

  private static final String TYPE_BASE = "https://api.devsu-bank.example/errores/";

  private final HttpStatus status;
  private final String title;

  ErrorCode(HttpStatus status, String title) {
    this.status = status;
    this.title = title;
  }

  public static ErrorCode of(DomainException ex) {
    return switch (ex) {
      case SaldoNoDisponibleException e -> SALDO_NO_DISPONIBLE;
      case DatoInvalidoException e -> DATO_INVALIDO;
      case RangoFechasInvalidoException e -> RANGO_FECHAS_INVALIDO;
      case CuentaNoEncontradaException e -> CUENTA_NO_ENCONTRADA;
      case MovimientoNoEncontradoException e -> MOVIMIENTO_NO_ENCONTRADO;
      case ClienteNoEncontradoException e -> CLIENTE_NO_ENCONTRADO;
      case NumeroCuentaDuplicadoException e -> NUMERO_CUENTA_DUPLICADO;
      case CuentaModificadaConcurrentementeException e -> CONFLICTO_DE_CONCURRENCIA;
      case MovimientoNoModificableException e -> MOVIMIENTO_NO_MODIFICABLE;
      case SaldoInicialNoModificableException e -> SALDO_INICIAL_NO_MODIFICABLE;
      case CuentaInactivaException e -> CUENTA_INACTIVA;
      case ClienteNoDisponibleException e -> CLIENTE_NO_DISPONIBLE;
      case ValorMovimientoInvalidoException e -> VALOR_MOVIMIENTO_INVALIDO;
      default -> REGLA_DE_NEGOCIO;
    };
  }

  /** Framework-raised errors (bad JSON, wrong verb, unknown route) mapped by status. */
  public static ErrorCode of(HttpStatusCode status) {
    return switch (status.value()) {
      case 400 -> CUERPO_INVALIDO;
      case 404 -> RECURSO_NO_ENCONTRADO;
      case 405 -> METODO_NO_PERMITIDO;
      case 415 -> TIPO_DE_CONTENIDO_NO_SOPORTADO;
      case 500 -> ERROR_INTERNO;
      default -> ERROR_HTTP;
    };
  }

  public HttpStatus status() {
    return status;
  }

  public String title() {
    return title;
  }

  public URI type() {
    return URI.create(TYPE_BASE + name().toLowerCase(Locale.ROOT).replace('_', '-'));
  }
}
