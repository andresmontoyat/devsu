package com.devsu.bank.cliente.infrastructure.driven.adapters.metrics;

import com.devsu.bank.cliente.application.command.UpdateClienteCommand;
import com.devsu.bank.cliente.application.port.inbound.UpdateClientePort;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.infrastructure.driven.adapters.metrics.support.ChainablePort;
import com.devsu.bank.cliente.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class UpdateClienteMetricsAdapter extends MetricRecorded
    implements UpdateClientePort, ChainablePort<UpdateClientePort> {

  private UpdateClientePort delegate;

  public UpdateClienteMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(UpdateClientePort delegate) {
    this.delegate = delegate;
  }

  @Override
  public Cliente execute(UpdateClienteCommand command) {
    return record("cliente", "update_cliente", () -> delegate.execute(command));
  }
}
