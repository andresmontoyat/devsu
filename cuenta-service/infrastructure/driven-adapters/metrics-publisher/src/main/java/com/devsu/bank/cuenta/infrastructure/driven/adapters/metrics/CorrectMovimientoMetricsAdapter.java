package com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics;

import com.devsu.bank.cuenta.application.command.CorrectMovimientoCommand;
import com.devsu.bank.cuenta.application.port.inbound.CorrectMovimientoPort;
import com.devsu.bank.cuenta.domain.model.ResultadoMovimiento;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.ChainablePort;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CorrectMovimientoMetricsAdapter extends MetricRecorded
    implements CorrectMovimientoPort, ChainablePort<CorrectMovimientoPort> {

  private CorrectMovimientoPort delegate;

  public CorrectMovimientoMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(CorrectMovimientoPort delegate) {
    this.delegate = delegate;
  }

  @Override
  public ResultadoMovimiento execute(CorrectMovimientoCommand command) {
    return record("movimiento", "correct_movimiento", () -> delegate.execute(command));
  }
}
