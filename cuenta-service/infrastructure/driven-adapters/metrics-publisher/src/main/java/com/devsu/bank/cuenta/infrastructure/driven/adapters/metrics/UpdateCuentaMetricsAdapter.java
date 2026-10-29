package com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics;

import com.devsu.bank.cuenta.application.command.UpdateCuentaCommand;
import com.devsu.bank.cuenta.application.port.inbound.UpdateCuentaPort;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.ChainablePort;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class UpdateCuentaMetricsAdapter extends MetricRecorded
    implements UpdateCuentaPort, ChainablePort<UpdateCuentaPort> {

  private UpdateCuentaPort delegate;

  public UpdateCuentaMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(UpdateCuentaPort delegate) {
    this.delegate = delegate;
  }

  @Override
  public Cuenta execute(UpdateCuentaCommand command) {
    return record("cuenta", "update_cuenta", () -> delegate.execute(command));
  }
}
