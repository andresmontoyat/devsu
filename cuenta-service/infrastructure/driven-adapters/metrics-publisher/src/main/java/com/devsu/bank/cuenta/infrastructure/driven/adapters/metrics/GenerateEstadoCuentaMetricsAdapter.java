package com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics;

import com.devsu.bank.cuenta.application.port.inbound.GenerateEstadoCuentaPort;
import com.devsu.bank.cuenta.application.query.EstadoCuentaQuery;
import com.devsu.bank.cuenta.domain.reporte.EstadoCuenta;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.ChainablePort;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GenerateEstadoCuentaMetricsAdapter extends MetricRecorded
    implements GenerateEstadoCuentaPort, ChainablePort<GenerateEstadoCuentaPort> {

  private GenerateEstadoCuentaPort delegate;

  public GenerateEstadoCuentaMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(GenerateEstadoCuentaPort delegate) {
    this.delegate = delegate;
  }

  @Override
  public EstadoCuenta execute(EstadoCuentaQuery query) {
    return record("reporte", "generate_estado_cuenta", () -> delegate.execute(query));
  }
}
