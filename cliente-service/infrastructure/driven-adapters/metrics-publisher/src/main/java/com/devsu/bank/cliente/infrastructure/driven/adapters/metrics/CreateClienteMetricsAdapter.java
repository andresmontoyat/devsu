package com.devsu.bank.cliente.infrastructure.driven.adapters.metrics;

import com.devsu.bank.cliente.application.command.CreateClienteCommand;
import com.devsu.bank.cliente.application.port.inbound.CreateClientePort;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.infrastructure.driven.adapters.metrics.support.ChainablePort;
import com.devsu.bank.cliente.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CreateClienteMetricsAdapter extends MetricRecorded
    implements CreateClientePort, ChainablePort<CreateClientePort> {

  private CreateClientePort delegate;

  public CreateClienteMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(CreateClientePort delegate) {
    this.delegate = delegate;
  }

  @Override
  public Cliente execute(CreateClienteCommand command) {
    return record("cliente", "create_cliente", () -> delegate.execute(command));
  }
}
