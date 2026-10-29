package com.devsu.bank.cliente.application.port.outbound;

import java.util.Optional;

/** W3C traceparent of the current request, captured so the dispatcher can continue the trace. */
@FunctionalInterface
public interface TraceIdSource {

  Optional<String> currentTraceparent();
}
