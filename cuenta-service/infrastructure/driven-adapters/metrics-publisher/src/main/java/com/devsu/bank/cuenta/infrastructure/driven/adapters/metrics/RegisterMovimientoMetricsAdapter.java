package com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics;

import com.devsu.bank.cuenta.application.command.RegisterMovimientoCommand;
import com.devsu.bank.cuenta.application.port.inbound.RegisterMovimientoPort;
import com.devsu.bank.cuenta.domain.model.ResultadoMovimiento;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.ChainablePort;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class RegisterMovimientoMetricsAdapter extends MetricRecorded
    implements RegisterMovimientoPort, ChainablePort<RegisterMovimientoPort> {

  private RegisterMovimientoPort delegate;

  public RegisterMovimientoMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(RegisterMovimientoPort delegate) {
    this.delegate = delegate;
  }

  @Override
  public ResultadoMovimiento execute(RegisterMovimientoCommand command) {
    return record("movimiento", "register_movimiento", () -> delegate.execute(command));
  }
}
