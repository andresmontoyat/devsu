package com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics;

import com.devsu.bank.cuenta.application.port.inbound.ListCuentasPort;
import com.devsu.bank.cuenta.application.query.CuentasQuery;
import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.ChainablePort;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ListCuentasMetricsAdapter extends MetricRecorded
    implements ListCuentasPort, ChainablePort<ListCuentasPort> {

  private ListCuentasPort delegate;

  public ListCuentasMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(ListCuentasPort delegate) {
    this.delegate = delegate;
  }

  @Override
  public Pagina<Cuenta> execute(CuentasQuery query) {
    return record("cuenta", "list_cuentas", () -> delegate.execute(query));
  }
}
