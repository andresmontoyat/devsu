package com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics;

import com.devsu.bank.cuenta.application.command.CreateCuentaCommand;
import com.devsu.bank.cuenta.application.port.inbound.CreateCuentaPort;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.ChainablePort;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CreateCuentaMetricsAdapter extends MetricRecorded
    implements CreateCuentaPort, ChainablePort<CreateCuentaPort> {

  private CreateCuentaPort delegate;

  public CreateCuentaMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(CreateCuentaPort delegate) {
    this.delegate = delegate;
  }

  @Override
  public Cuenta execute(CreateCuentaCommand command) {
    return record("cuenta", "create_cuenta", () -> delegate.execute(command));
  }
}
