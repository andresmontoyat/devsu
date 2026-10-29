package com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.error;

import com.devsu.bank.cliente.domain.exception.DatoInvalidoException;
import com.devsu.bank.cliente.domain.exception.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Single place where exceptions become HTTP: RFC 9457 Problem Details, never a stack trace. */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ProblemDetail> handleDomain(DomainException ex, HttpServletRequest req) {
    ErrorCode code = ErrorCode.of(ex);
    ProblemDetail problem = ProblemDetailFactory.create(code, ex.getMessage(), req.getRequestURI());
    if (ex instanceof DatoInvalidoException invalido) {
      ProblemDetailFactory.withErrors(
          problem,
          List.of(new ProblemDetailFactory.FieldError(invalido.getCampo(), ex.getMessage())));
    }
    log.debug("Peticion rechazada [{}]: {}", code, ex.getMessage());
    return ResponseEntity.status(code.status()).body(problem);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest req) {
    log.error("Error no controlado en {}", req.getRequestURI(), ex);
    ProblemDetail problem =
        ProblemDetailFactory.create(
            ErrorCode.ERROR_INTERNO, "Ocurrio un error inesperado", req.getRequestURI());
    return ResponseEntity.internalServerError().body(problem);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    List<ProblemDetailFactory.FieldError> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> new ProblemDetailFactory.FieldError(e.getField(), e.getDefaultMessage()))
            .toList();
    ProblemDetail problem =
        ProblemDetailFactory.create(
            ErrorCode.VALIDACION_FALLIDA, "La peticion tiene campos invalidos", path(request));
    return ResponseEntity.badRequest().body(ProblemDetailFactory.withErrors(problem, errors));
  }

  @Override
  protected ResponseEntity<Object> handleTypeMismatch(
      TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    ProblemDetail problem =
        ProblemDetailFactory.create(
            ErrorCode.PARAMETRO_INVALIDO,
            "Valor invalido para '" + ex.getPropertyName() + "'",
            path(request));
    return ResponseEntity.badRequest().body(problem);
  }

  /** Framework errors (bad JSON, wrong verb, unknown route) get the same envelope. */
  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    ProblemDetail problem =
        body instanceof ProblemDetail existing ? existing : ProblemDetail.forStatus(status);
    ProblemDetailFactory.enrich(problem, ErrorCode.of(status), path(request));
    return super.handleExceptionInternal(ex, problem, headers, status, request);
  }

  private static String path(WebRequest request) {
    return request instanceof ServletWebRequest servlet
        ? servlet.getRequest().getRequestURI()
        : null;
  }
}
