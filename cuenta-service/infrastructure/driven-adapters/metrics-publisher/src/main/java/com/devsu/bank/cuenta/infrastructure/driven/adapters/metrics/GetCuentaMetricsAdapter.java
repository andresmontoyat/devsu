package com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics;

import com.devsu.bank.cuenta.application.port.inbound.GetCuentaPort;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.ChainablePort;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GetCuentaMetricsAdapter extends MetricRecorded
    implements GetCuentaPort, ChainablePort<GetCuentaPort> {

  private GetCuentaPort delegate;

  public GetCuentaMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(GetCuentaPort delegate) {
    this.delegate = delegate;
  }

  @Override
  public Cuenta execute(NumeroCuenta numeroCuenta) {
    return record("cuenta", "get_cuenta", () -> delegate.execute(numeroCuenta));
  }
}
