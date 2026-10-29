package com.devsu.bank.cliente.infrastructure.driven.adapters.metrics;

import com.devsu.bank.cliente.application.port.inbound.ListClientesPort;
import com.devsu.bank.cliente.application.query.Pagina;
import com.devsu.bank.cliente.application.query.PaginaSolicitud;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.infrastructure.driven.adapters.metrics.support.ChainablePort;
import com.devsu.bank.cliente.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ListClientesMetricsAdapter extends MetricRecorded
    implements ListClientesPort, ChainablePort<ListClientesPort> {

  private ListClientesPort delegate;

  public ListClientesMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(ListClientesPort delegate) {
    this.delegate = delegate;
  }

  @Override
  public Pagina<Cliente> execute(PaginaSolicitud solicitud) {
    return record("cliente", "list_clientes", () -> delegate.execute(solicitud));
  }
}
