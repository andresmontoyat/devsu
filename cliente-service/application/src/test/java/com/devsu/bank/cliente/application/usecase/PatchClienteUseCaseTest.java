package com.devsu.bank.cliente.application.usecase;

import static com.devsu.bank.cliente.application.support.ClienteTestData.CLIENTE_ID;
import static com.devsu.bank.cliente.application.support.ClienteTestData.joseLemaPersistido;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cliente.application.command.PatchClienteCommand;
import com.devsu.bank.cliente.application.event.ClienteOutboxEvents;
import com.devsu.bank.cliente.application.support.RecordingPasswordHasher;
import com.devsu.bank.cliente.application.support.UseCaseHarness;
import com.devsu.bank.cliente.domain.exception.ClienteNoEncontradoException;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.Genero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PatchClienteUseCaseTest {

  private UseCaseHarness harness;
  private PatchClienteUseCase useCase;

  @BeforeEach
  void setUp() {
    harness = new UseCaseHarness();
    harness.repository.guardarExistente(joseLemaPersistido());
    useCase =
        new PatchClienteUseCase(
            harness.transaction(), harness.repository, harness.hasher, harness.events);
  }

  @Test
  void should_change_only_the_given_fields() {
    PatchClienteCommand soloDireccion =
        PatchClienteCommand.builder(CLIENTE_ID).direccion("Amazonas y NNUU").build();

    Cliente actualizado = useCase.execute(soloDireccion);

    assertThat(actualizado.getDireccion()).isEqualTo("Amazonas y NNUU");
    assertThat(actualizado.getNombre()).isEqualTo(joseLemaPersistido().getNombre());
    assertThat(actualizado.getTelefono()).isEqualTo(joseLemaPersistido().getTelefono());
    assertThat(actualizado.isActivo()).isTrue();
  }

  @Test
  void should_apply_every_personal_field_when_all_are_given() {
    PatchClienteCommand todo =
        PatchClienteCommand.builder(CLIENTE_ID)
            .nombre("Jose A. Lema")
            .genero("OTRO")
            .edad(40)
            .direccion("13 junio y Equinoccial")
            .telefono("098874587")
            .build();

    Cliente actualizado = useCase.execute(todo);

    assertThat(actualizado.getNombre()).isEqualTo("Jose A. Lema");
    assertThat(actualizado.getGenero()).isEqualTo(Genero.OTRO);
    assertThat(actualizado.getEdad()).isEqualTo(40);
    assertThat(actualizado.getTelefono().value()).isEqualTo("098874587");
  }

  @Test
  void should_deactivate_the_client_when_only_the_state_is_given() {
    Cliente actualizado =
        useCase.execute(PatchClienteCommand.builder(CLIENTE_ID).estado(false).build());

    assertThat(actualizado.isActivo()).isFalse();
    assertThat(actualizado.getDatos()).isEqualTo(joseLemaPersistido().getDatos());
  }

  @Test
  void should_replace_the_password_when_only_the_password_is_given() {
    Cliente actualizado =
        useCase.execute(PatchClienteCommand.builder(CLIENTE_ID).contrasena("otra-clave").build());

    assertThat(actualizado.getContrasena()).isEqualTo(RecordingPasswordHasher.hashDe("otra-clave"));
    assertThat(harness.hasher.fueLlamadoDentroDeTransaccion()).isFalse();
  }

  @Test
  void should_write_ClienteActualizado_in_the_same_transaction() {
    useCase.execute(PatchClienteCommand.builder(CLIENTE_ID).estado(false).build());

    assertThat(harness.outbox.todosDentroDeTransaccion()).isTrue();
    assertThat(harness.outbox.unicoEvento().body().eventName())
        .isEqualTo(ClienteOutboxEvents.CLIENTE_ACTUALIZADO);
  }

  @Test
  void should_fail_with_not_found_when_the_client_does_not_exist() {
    harness.repository.delete(CLIENTE_ID);

    assertThatThrownBy(
            () -> useCase.execute(PatchClienteCommand.builder(CLIENTE_ID).estado(false).build()))
        .isInstanceOf(ClienteNoEncontradoException.class);
    assertThat(harness.outbox.mensajes()).isEmpty();
  }
}
