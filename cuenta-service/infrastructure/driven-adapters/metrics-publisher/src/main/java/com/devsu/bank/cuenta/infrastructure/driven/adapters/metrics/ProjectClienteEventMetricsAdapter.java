package com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics;

import com.devsu.bank.cuenta.application.command.ReceivedClienteEvent;
import com.devsu.bank.cuenta.application.port.inbound.ProjectClienteEventPort;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.ChainablePort;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.MetricRecorded;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ProjectClienteEventMetricsAdapter extends MetricRecorded
    implements ProjectClienteEventPort, ChainablePort<ProjectClienteEventPort> {

  private ProjectClienteEventPort delegate;

  public ProjectClienteEventMetricsAdapter(MeterRegistry registry) {
    super(registry);
  }

  @Override
  public void setDelegate(ProjectClienteEventPort delegate) {
    this.delegate = delegate;
  }

  @Override
  public void execute(ReceivedClienteEvent event) {
    recordVoid("cliente_evento", "project_cliente_event", () -> delegate.execute(event));
  }
}
