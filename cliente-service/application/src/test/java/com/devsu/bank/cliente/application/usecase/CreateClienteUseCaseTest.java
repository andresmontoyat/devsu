package com.devsu.bank.cliente.application.usecase;

import static com.devsu.bank.cliente.application.support.ClienteTestData.crearJoseLema;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cliente.application.command.CreateClienteCommand;
import com.devsu.bank.cliente.application.event.ClienteEventPayload;
import com.devsu.bank.cliente.application.event.ClienteOutboxEvents;
import com.devsu.bank.cliente.application.outbox.EventMessage;
import com.devsu.bank.cliente.application.support.ClienteTestData;
import com.devsu.bank.cliente.application.support.RecordingPasswordHasher;
import com.devsu.bank.cliente.application.support.UseCaseHarness;
import com.devsu.bank.cliente.domain.exception.DatoInvalidoException;
import com.devsu.bank.cliente.domain.exception.IdentificacionDuplicadaException;
import com.devsu.bank.cliente.domain.model.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateClienteUseCaseTest {

  private UseCaseHarness harness;
  private CreateClienteUseCase useCase;

  @BeforeEach
  void setUp() {
    harness = new UseCaseHarness();
    useCase =
        new CreateClienteUseCase(
            harness.transaction(), harness.repository, harness.hasher, harness.events);
  }

  @Test
  void should_persist_an_active_client_with_hashed_password_when_command_is_valid() {
    Cliente creado = useCase.execute(crearJoseLema());

    assertThat(harness.repository.findById(creado.getClienteId())).contains(creado);
    assertThat(creado.isActivo()).isTrue();
    assertThat(creado.getNombre()).isEqualTo("Jose Lema");
    assertThat(creado.getContrasena())
        .isEqualTo(RecordingPasswordHasher.hashDe(ClienteTestData.CONTRASENA));
  }

  @Test
  void should_create_an_inactive_client_when_the_command_says_so() {
    CreateClienteCommand base = crearJoseLema();
    CreateClienteCommand inactivo =
        new CreateClienteCommand(
            base.nombre(),
            base.genero(),
            base.edad(),
            base.identificacion(),
            base.direccion(),
            base.telefono(),
            base.contrasena(),
            false);

    assertThat(useCase.execute(inactivo).isActivo()).isFalse();
  }

  @Test
  void should_default_to_active_when_the_command_omits_the_state() {
    CreateClienteCommand base = crearJoseLema();
    CreateClienteCommand sinEstado =
        new CreateClienteCommand(
            base.nombre(),
            base.genero(),
            base.edad(),
            base.identificacion(),
            base.direccion(),
            base.telefono(),
            base.contrasena(),
            null);

    assertThat(useCase.execute(sinEstado).isActivo()).isTrue();
  }

  @Test
  void should_write_ClienteCreado_to_the_outbox_in_the_same_transaction() {
    Cliente creado = useCase.execute(crearJoseLema());

    EventMessage<?> evento = harness.outbox.unicoEvento();
    assertThat(harness.outbox.todosDentroDeTransaccion()).isTrue();
    assertThat(evento.body().eventName()).isEqualTo(ClienteOutboxEvents.CLIENTE_CREADO);
    assertThat(evento.body().aggregate()).isEqualTo(creado.getClienteId().value().toString());
    assertThat(evento.body().payload())
        .isEqualTo(
            new ClienteEventPayload(
                creado.getClienteId().value().toString(), "Jose Lema", true, 0L));
  }

  @Test
  void should_hash_the_password_outside_the_transaction() {
    useCase.execute(crearJoseLema());

    assertThat(harness.hasher.fueLlamadoDentroDeTransaccion()).isFalse();
  }

  @Test
  void should_reject_and_write_nothing_when_the_identification_already_exists() {
    useCase.execute(crearJoseLema());

    assertThatThrownBy(() -> useCase.execute(crearJoseLema()))
        .isInstanceOf(IdentificacionDuplicadaException.class);
    assertThat(harness.repository.cantidad()).isEqualTo(1);
    assertThat(harness.outbox.mensajes()).hasSize(1);
  }

  @Test
  void should_reject_and_write_nothing_when_the_data_is_invalid() {
    CreateClienteCommand base = crearJoseLema();
    CreateClienteCommand sinNombre =
        new CreateClienteCommand(
            " ",
            base.genero(),
            base.edad(),
            base.identificacion(),
            base.direccion(),
            base.telefono(),
            base.contrasena(),
            true);

    assertThatThrownBy(() -> useCase.execute(sinNombre)).isInstanceOf(DatoInvalidoException.class);
    assertThat(harness.repository.cantidad()).isZero();
    assertThat(harness.outbox.mensajes()).isEmpty();
  }
}
