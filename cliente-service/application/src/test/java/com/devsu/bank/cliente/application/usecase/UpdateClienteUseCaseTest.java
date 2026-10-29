package com.devsu.bank.cliente.application.usecase;

import static com.devsu.bank.cliente.application.support.ClienteTestData.CLIENTE_ID;
import static com.devsu.bank.cliente.application.support.ClienteTestData.IDENTIFICACION;
import static com.devsu.bank.cliente.application.support.ClienteTestData.VERSION_PERSISTIDA;
import static com.devsu.bank.cliente.application.support.ClienteTestData.joseLemaPersistido;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cliente.application.command.UpdateClienteCommand;
import com.devsu.bank.cliente.application.event.ClienteEventPayload;
import com.devsu.bank.cliente.application.event.ClienteOutboxEvents;
import com.devsu.bank.cliente.application.support.RecordingPasswordHasher;
import com.devsu.bank.cliente.application.support.UseCaseHarness;
import com.devsu.bank.cliente.domain.exception.ClienteNoEncontradoException;
import com.devsu.bank.cliente.domain.exception.IdentificacionInmutableException;
import com.devsu.bank.cliente.domain.model.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateClienteUseCaseTest {

  private UseCaseHarness harness;
  private UpdateClienteUseCase useCase;

  @BeforeEach
  void setUp() {
    harness = new UseCaseHarness();
    harness.repository.guardarExistente(joseLemaPersistido());
    useCase =
        new UpdateClienteUseCase(
            harness.transaction(), harness.repository, harness.hasher, harness.events);
  }

  @Test
  void should_replace_the_personal_data_and_state_when_the_client_exists() {
    Cliente actualizado = useCase.execute(comando(IDENTIFICACION, null, false));

    assertThat(actualizado.getNombre()).isEqualTo("Jose Lema Actualizado");
    assertThat(actualizado.getDireccion()).isEqualTo("Amazonas y NNUU");
    assertThat(actualizado.isActivo()).isFalse();
    assertThat(actualizado.getVersion()).isEqualTo(VERSION_PERSISTIDA + 1);
  }

  @Test
  void should_keep_the_current_password_when_none_is_given() {
    Cliente actualizado = useCase.execute(comando(IDENTIFICACION, null, true));

    assertThat(actualizado.getContrasena()).isEqualTo(joseLemaPersistido().getContrasena());
  }

  @Test
  void should_hash_and_replace_the_password_outside_the_transaction_when_given() {
    Cliente actualizado = useCase.execute(comando(IDENTIFICACION, "nueva-clave", true));

    assertThat(actualizado.getContrasena())
        .isEqualTo(RecordingPasswordHasher.hashDe("nueva-clave"));
    assertThat(harness.hasher.fueLlamadoDentroDeTransaccion()).isFalse();
  }

  @Test
  void should_write_ClienteActualizado_with_the_new_version_in_the_same_transaction() {
    useCase.execute(comando(IDENTIFICACION, null, false));

    assertThat(harness.outbox.todosDentroDeTransaccion()).isTrue();
    assertThat(harness.outbox.unicoEvento().body().eventName())
        .isEqualTo(ClienteOutboxEvents.CLIENTE_ACTUALIZADO);
    assertThat(harness.outbox.unicoEvento().body().payload())
        .isEqualTo(
            new ClienteEventPayload(
                CLIENTE_ID.value().toString(),
                "Jose Lema Actualizado",
                false,
                VERSION_PERSISTIDA + 1));
  }

  @Test
  void should_fail_with_not_found_and_write_nothing_when_the_client_does_not_exist() {
    harness.repository.delete(CLIENTE_ID);

    assertThatThrownBy(() -> useCase.execute(comando(IDENTIFICACION, null, true)))
        .isInstanceOf(ClienteNoEncontradoException.class);
    assertThat(harness.outbox.mensajes()).isEmpty();
  }

  @Test
  void should_reject_and_write_nothing_when_the_identification_changes() {
    assertThatThrownBy(() -> useCase.execute(comando("0999999999", null, true)))
        .isInstanceOf(IdentificacionInmutableException.class);
    assertThat(harness.outbox.mensajes()).isEmpty();
  }

  private static UpdateClienteCommand comando(
      String identificacion, String contrasena, boolean estado) {
    return new UpdateClienteCommand(
        CLIENTE_ID,
        "Jose Lema Actualizado",
        "MASCULINO",
        36,
        identificacion,
        "Amazonas y NNUU",
        "098254785",
        contrasena,
        estado);
  }
}
