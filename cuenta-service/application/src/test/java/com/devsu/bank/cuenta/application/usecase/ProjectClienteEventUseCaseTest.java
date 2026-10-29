package com.devsu.bank.cuenta.application.usecase;

import static com.devsu.bank.cuenta.application.command.ReceivedClienteEvent.CLIENTE_ACTUALIZADO;
import static com.devsu.bank.cuenta.application.command.ReceivedClienteEvent.CLIENTE_CREADO;
import static com.devsu.bank.cuenta.application.command.ReceivedClienteEvent.CLIENTE_ELIMINADO;
import static com.devsu.bank.cuenta.application.support.CuentaHarness.MARIANELA;
import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.bank.cuenta.application.command.ReceivedClienteEvent;
import com.devsu.bank.cuenta.application.support.CuentaHarness;
import com.devsu.bank.cuenta.domain.model.ClienteProyeccion;
import com.devsu.bank.cuenta.domain.model.TipoCuenta;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProjectClienteEventUseCaseTest {

  private static final String ID = MARIANELA.value().toString();

  private CuentaHarness harness;
  private ProjectClienteEventUseCase useCase;

  @BeforeEach
  void setUp() {
    harness = new CuentaHarness();
    useCase =
        new ProjectClienteEventUseCase(
            harness.transaction(), harness.clientes, harness.cuentas, harness.procesados);
  }

  @Test
  void should_create_the_local_copy_when_a_client_is_created() {
    useCase.execute(evento(CLIENTE_CREADO, "Marianela Montalvo", true, 0));

    assertThat(proyeccion())
        .isEqualTo(ClienteProyeccion.desde(MARIANELA, "Marianela Montalvo", true, 0));
  }

  @Test
  void should_apply_a_newer_update_and_ignore_a_stale_one() {
    useCase.execute(evento(CLIENTE_CREADO, "Marianela", true, 0));
    useCase.execute(evento(CLIENTE_ACTUALIZADO, "Marianela Montalvo", false, 2));
    useCase.execute(evento(CLIENTE_ACTUALIZADO, "Viejo", true, 1));

    assertThat(proyeccion().nombre()).isEqualTo("Marianela Montalvo");
    assertThat(proyeccion().activo()).isFalse();
  }

  @Test
  void should_process_a_redelivered_message_only_once() {
    UUID mensaje = UUID.randomUUID();
    useCase.execute(new ReceivedClienteEvent(mensaje, CLIENTE_CREADO, ID, "Marianela", true, 0));
    harness.clientes.save(ClienteProyeccion.desde(MARIANELA, "Cambiado localmente", true, 0));

    useCase.execute(new ReceivedClienteEvent(mensaje, CLIENTE_CREADO, ID, "Marianela", true, 0));

    assertThat(proyeccion().nombre()).isEqualTo("Cambiado localmente");
  }

  @Test
  void should_create_from_an_update_that_arrives_before_the_creation() {
    useCase.execute(evento(CLIENTE_ACTUALIZADO, "Marianela Montalvo", true, 3));

    assertThat(proyeccion().version()).isEqualTo(3);
  }

  @Test
  void should_tombstone_the_client_and_deactivate_its_accounts_when_deleted() {
    useCase.execute(evento(CLIENTE_CREADO, "Marianela", true, 0));
    harness.abrir("225487", TipoCuenta.CORRIENTE, "100", MARIANELA);

    useCase.execute(evento(CLIENTE_ELIMINADO, null, null, 1));

    assertThat(proyeccion().eliminado()).isTrue();
    assertThat(harness.cuentas.get("225487").isActiva()).isFalse();
    assertThat(harness.probe.runs()).isEqualTo(2);
  }

  @Test
  void should_keep_a_deletion_that_arrives_before_the_creation() {
    useCase.execute(evento(CLIENTE_ELIMINADO, null, null, 5));
    useCase.execute(evento(CLIENTE_CREADO, "Marianela", true, 0));

    assertThat(proyeccion().eliminado()).isTrue();
  }

  @Test
  void should_ignore_unknown_event_types_but_remember_them() {
    ReceivedClienteEvent desconocido =
        new ReceivedClienteEvent(UUID.randomUUID(), "ClienteFusionado", ID, "X", true, 1);

    useCase.execute(desconocido);

    assertThat(harness.clientes.findById(MARIANELA)).isEmpty();
    assertThat(harness.procesados.contiene(desconocido.messageId())).isTrue();
  }

  private ClienteProyeccion proyeccion() {
    return harness.clientes.findById(MARIANELA).orElseThrow();
  }

  private static ReceivedClienteEvent evento(
      String tipo, String nombre, Boolean estado, long version) {
    return new ReceivedClienteEvent(UUID.randomUUID(), tipo, ID, nombre, estado, version);
  }
}
