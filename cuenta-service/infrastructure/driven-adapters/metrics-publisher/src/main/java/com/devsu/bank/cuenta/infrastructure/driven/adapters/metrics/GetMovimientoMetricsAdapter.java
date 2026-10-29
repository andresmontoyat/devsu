package com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics;

import com.devsu.bank.cuenta.application.port.inbound.GetMovimientoPort;
import com.devsu.bank.cuenta.domain.model.Movimiento;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.ChainablePort;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GetMovimientoMetricsAdapter extends MetricRecorded
    implements GetMovimientoPort, ChainablePort<GetMovimientoPort> {

  private GetMovimientoPort delegate;

  public GetMovimientoMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(GetMovimientoPort delegate) {
    this.delegate = delegate;
  }

  @Override
  public Movimiento execute(MovimientoId movimientoId) {
    return record("movimiento", "get_movimiento", () -> delegate.execute(movimientoId));
  }
}
