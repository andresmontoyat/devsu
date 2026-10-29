package com.devsu.bank.cliente.config;

import com.devsu.bank.cliente.application.port.outbound.TraceIdSource;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import java.time.Clock;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class PlatformConfig {

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  /** W3C traceparent of the current span, stored with each outbox row to continue the trace. */
  @Bean
  TraceIdSource traceIdSource(ObjectProvider<Tracer> tracer) {
    return () ->
        Optional.ofNullable(tracer.getIfAvailable())
            .map(Tracer::currentSpan)
            .map(Span::context)
            .map(PlatformConfig::traceparent);
  }

  private static String traceparent(TraceContext context) {
    String flags = Boolean.TRUE.equals(context.sampled()) ? "01" : "00";
    return "00-" + context.traceId() + "-" + context.spanId() + "-" + flags;
  }
}
