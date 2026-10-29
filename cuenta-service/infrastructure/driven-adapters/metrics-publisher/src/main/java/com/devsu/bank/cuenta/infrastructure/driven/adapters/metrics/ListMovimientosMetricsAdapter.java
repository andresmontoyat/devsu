package com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics;

import com.devsu.bank.cuenta.application.port.inbound.ListMovimientosPort;
import com.devsu.bank.cuenta.application.query.MovimientosQuery;
import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.domain.model.Movimiento;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.ChainablePort;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ListMovimientosMetricsAdapter extends MetricRecorded
    implements ListMovimientosPort, ChainablePort<ListMovimientosPort> {

  private ListMovimientosPort delegate;

  public ListMovimientosMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(ListMovimientosPort delegate) {
    this.delegate = delegate;
  }

  @Override
  public Pagina<Movimiento> execute(MovimientosQuery query) {
    return record("movimiento", "list_movimientos", () -> delegate.execute(query));
  }
}
