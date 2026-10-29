package com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics;

import com.devsu.bank.cuenta.application.command.PatchCuentaCommand;
import com.devsu.bank.cuenta.application.port.inbound.PatchCuentaPort;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.ChainablePort;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class PatchCuentaMetricsAdapter extends MetricRecorded
    implements PatchCuentaPort, ChainablePort<PatchCuentaPort> {

  private PatchCuentaPort delegate;

  public PatchCuentaMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(PatchCuentaPort delegate) {
    this.delegate = delegate;
  }

  @Override
  public Cuenta execute(PatchCuentaCommand command) {
    return record("cuenta", "patch_cuenta", () -> delegate.execute(command));
  }
}
