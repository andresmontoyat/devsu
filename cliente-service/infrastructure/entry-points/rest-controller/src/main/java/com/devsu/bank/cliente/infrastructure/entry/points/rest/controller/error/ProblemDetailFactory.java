package com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.error;

import java.net.URI;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.http.ProblemDetail;

/** Builds the RFC 9457 body with the extra members of api-design.md §7: code, traceId, errors. */
public final class ProblemDetailFactory {

  private ProblemDetailFactory() {}

  public static ProblemDetail create(ErrorCode code, String detail, String instance) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(code.status(), detail);
    return enrich(problem, code, instance);
  }

  public static ProblemDetail enrich(ProblemDetail problem, ErrorCode code, String instance) {
    problem.setType(code.type());
    problem.setTitle(code.title());
    if (instance != null) {
      problem.setInstance(URI.create(instance));
    }
    problem.setProperty("code", code.name());
    problem.setProperty("traceId", MDC.get("traceId"));
    return problem;
  }

  public static ProblemDetail withErrors(ProblemDetail problem, List<FieldError> errors) {
    problem.setProperty("errors", errors);
    return problem;
  }

  public record FieldError(String field, String message) {}
}
