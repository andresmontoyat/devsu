package com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.error;

import com.devsu.bank.cliente.domain.exception.ClienteMenorDeEdadException;
import com.devsu.bank.cliente.domain.exception.ClienteModificadoConcurrentementeException;
import com.devsu.bank.cliente.domain.exception.ClienteNoEncontradoException;
import com.devsu.bank.cliente.domain.exception.DatoInvalidoException;
import com.devsu.bank.cliente.domain.exception.DomainException;
import com.devsu.bank.cliente.domain.exception.IdentificacionDuplicadaException;
import com.devsu.bank.cliente.domain.exception.IdentificacionInmutableException;
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
  CLIENTE_NO_ENCONTRADO(HttpStatus.NOT_FOUND, "Cliente no encontrado"),
  RECURSO_NO_ENCONTRADO(HttpStatus.NOT_FOUND, "Recurso no encontrado"),
  METODO_NO_PERMITIDO(HttpStatus.METHOD_NOT_ALLOWED, "Metodo no permitido"),
  TIPO_DE_CONTENIDO_NO_SOPORTADO(
      HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de contenido no soportado"),
  IDENTIFICACION_DUPLICADA(HttpStatus.CONFLICT, "Identificacion duplicada"),
  CONFLICTO_DE_CONCURRENCIA(HttpStatus.CONFLICT, "Conflicto de concurrencia"),
  IDENTIFICACION_INMUTABLE(HttpStatus.UNPROCESSABLE_CONTENT, "Identificacion inmutable"),
  CLIENTE_MENOR_DE_EDAD(HttpStatus.UNPROCESSABLE_CONTENT, "Cliente menor de edad"),
  REGLA_DE_NEGOCIO(HttpStatus.UNPROCESSABLE_CONTENT, "Regla de negocio incumplida"),
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
      case DatoInvalidoException e -> DATO_INVALIDO;
      case ClienteNoEncontradoException e -> CLIENTE_NO_ENCONTRADO;
      case IdentificacionDuplicadaException e -> IDENTIFICACION_DUPLICADA;
      case ClienteModificadoConcurrentementeException e -> CONFLICTO_DE_CONCURRENCIA;
      case IdentificacionInmutableException e -> IDENTIFICACION_INMUTABLE;
      case ClienteMenorDeEdadException e -> CLIENTE_MENOR_DE_EDAD;
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
