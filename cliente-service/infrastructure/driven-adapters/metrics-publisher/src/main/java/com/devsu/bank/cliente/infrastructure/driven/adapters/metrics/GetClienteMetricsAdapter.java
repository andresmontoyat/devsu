package com.devsu.bank.cliente.infrastructure.driven.adapters.metrics;

import com.devsu.bank.cliente.application.port.inbound.GetClientePort;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ClienteId;
import com.devsu.bank.cliente.infrastructure.driven.adapters.metrics.support.ChainablePort;
import com.devsu.bank.cliente.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GetClienteMetricsAdapter extends MetricRecorded
    implements GetClientePort, ChainablePort<GetClientePort> {

  private GetClientePort delegate;

  public GetClienteMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(GetClientePort delegate) {
    this.delegate = delegate;
  }

  @Override
  public Cliente execute(ClienteId clienteId) {
    return record("cliente", "get_cliente", () -> delegate.execute(clienteId));
  }
}
