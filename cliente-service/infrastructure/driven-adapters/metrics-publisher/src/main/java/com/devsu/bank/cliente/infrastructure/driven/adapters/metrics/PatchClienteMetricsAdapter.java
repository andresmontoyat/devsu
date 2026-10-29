package com.devsu.bank.cliente.infrastructure.driven.adapters.metrics;

import com.devsu.bank.cliente.application.command.PatchClienteCommand;
import com.devsu.bank.cliente.application.port.inbound.PatchClientePort;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.infrastructure.driven.adapters.metrics.support.ChainablePort;
import com.devsu.bank.cliente.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class PatchClienteMetricsAdapter extends MetricRecorded
    implements PatchClientePort, ChainablePort<PatchClientePort> {

  private PatchClientePort delegate;

  public PatchClienteMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(PatchClientePort delegate) {
    this.delegate = delegate;
  }

  @Override
  public Cliente execute(PatchClienteCommand command) {
    return record("cliente", "patch_cliente", () -> delegate.execute(command));
  }
}
