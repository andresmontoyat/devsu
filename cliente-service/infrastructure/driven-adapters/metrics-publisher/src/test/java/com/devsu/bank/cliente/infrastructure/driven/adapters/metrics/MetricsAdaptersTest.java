package com.devsu.bank.cliente.infrastructure.driven.adapters.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cliente.application.command.CreateClienteCommand;
import com.devsu.bank.cliente.application.command.PatchClienteCommand;
import com.devsu.bank.cliente.application.command.UpdateClienteCommand;
import com.devsu.bank.cliente.application.query.Pagina;
import com.devsu.bank.cliente.application.query.PaginaSolicitud;
import com.devsu.bank.cliente.domain.exception.ClienteNoEncontradoException;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ClienteId;
import com.devsu.bank.cliente.domain.model.ContrasenaHash;
import com.devsu.bank.cliente.domain.model.DatosPersona;
import com.devsu.bank.cliente.domain.model.Genero;
import com.devsu.bank.cliente.domain.model.Identificacion;
import com.devsu.bank.cliente.domain.model.Telefono;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetricsAdaptersTest {

  private static final ClienteId ID = ClienteId.of("0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e01");
  private static final Cliente CLIENTE =
      Cliente.registrar(
          ID,
          new DatosPersona(
              "Jose",
              Genero.MASCULINO,
              35,
              new Identificacion("1712345678"),
              "Otavalo",
              new Telefono("098254785")),
          new ContrasenaHash("$2a$10$hash"));

  private SimpleMeterRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new SimpleMeterRegistry();
  }

  @Test
  void should_time_and_count_a_successful_call_and_return_the_delegate_result() {
    CreateClienteMetricsAdapter adapter = new CreateClienteMetricsAdapter(registry);
    adapter.setDelegate(command -> CLIENTE);

    Cliente resultado = adapter.execute(crearCommand());

    assertThat(resultado).isSameAs(CLIENTE);
    assertThat(llamadas("create_cliente", "true")).isEqualTo(1.0);
    assertThat(registry.get("usecase.operation").tag("operation", "create_cliente").timer().count())
        .isEqualTo(1);
  }

  @Test
  void should_count_the_error_by_type_and_rethrow_it() {
    GetClienteMetricsAdapter adapter = new GetClienteMetricsAdapter(registry);
    adapter.setDelegate(
        id -> {
          throw new ClienteNoEncontradoException(id);
        });

    assertThatThrownBy(() -> adapter.execute(ID)).isInstanceOf(ClienteNoEncontradoException.class);
    assertThat(llamadas("get_cliente", "false")).isEqualTo(1.0);
    assertThat(
            registry
                .get("usecase.operation.errors")
                .tag("error", "ClienteNoEncontradoException")
                .counter()
                .count())
        .isEqualTo(1.0);
  }

  @Test
  void should_record_every_client_operation() {
    ListClientesMetricsAdapter list = new ListClientesMetricsAdapter(registry);
    list.setDelegate(s -> new Pagina<>(List.of(), s, 0));
    UpdateClienteMetricsAdapter update = new UpdateClienteMetricsAdapter(registry);
    update.setDelegate(c -> CLIENTE);
    PatchClienteMetricsAdapter patch = new PatchClienteMetricsAdapter(registry);
    patch.setDelegate(c -> CLIENTE);
    DeleteClienteMetricsAdapter delete = new DeleteClienteMetricsAdapter(registry);
    delete.setDelegate(id -> {});

    list.execute(new PaginaSolicitud(0, 10));
    update.execute(
        new UpdateClienteCommand(
            ID, "J", "MASCULINO", 30, "1712345678", "D", "098254785", null, true));
    patch.execute(PatchClienteCommand.builder(ID).estado(false).build());
    delete.execute(ID);

    assertThat(llamadas("list_clientes", "true")).isEqualTo(1.0);
    assertThat(llamadas("update_cliente", "true")).isEqualTo(1.0);
    assertThat(llamadas("patch_cliente", "true")).isEqualTo(1.0);
    assertThat(llamadas("delete_cliente", "true")).isEqualTo(1.0);
  }

  @Test
  void should_run_first_in_the_chain() {
    assertThat(new CreateClienteMetricsAdapter(registry).getOrder())
        .isEqualTo(
            com.devsu.bank.cliente.infrastructure.driven.adapters.metrics.support.MetricRecorded
                .ORDER);
  }

  private double llamadas(String operation, String success) {
    return registry
        .get("usecase.operation.calls")
        .tag("entity", "cliente")
        .tag("operation", operation)
        .tag("success", success)
        .counter()
        .count();
  }

  private static CreateClienteCommand crearCommand() {
    return new CreateClienteCommand(
        "Jose", "MASCULINO", 35, "1712345678", "Otavalo", "098254785", "1234", true);
  }
}
