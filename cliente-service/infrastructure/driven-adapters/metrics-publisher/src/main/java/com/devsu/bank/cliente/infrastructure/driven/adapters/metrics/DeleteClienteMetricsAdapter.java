package com.devsu.bank.cliente.infrastructure.driven.adapters.metrics;

import com.devsu.bank.cliente.application.port.inbound.DeleteClientePort;
import com.devsu.bank.cliente.domain.model.ClienteId;
import com.devsu.bank.cliente.infrastructure.driven.adapters.metrics.support.ChainablePort;
import com.devsu.bank.cliente.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class DeleteClienteMetricsAdapter extends MetricRecorded
    implements DeleteClientePort, ChainablePort<DeleteClientePort> {

  private DeleteClientePort delegate;

  public DeleteClienteMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(DeleteClientePort delegate) {
    this.delegate = delegate;
  }

  @Override
  public void execute(ClienteId clienteId) {
    recordVoid("cliente", "delete_cliente", () -> delegate.execute(clienteId));
  }
}
