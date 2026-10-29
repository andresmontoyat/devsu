package com.devsu.bank.cliente.application.usecase;

import static com.devsu.bank.cliente.application.support.ClienteTestData.CLIENTE_ID;
import static com.devsu.bank.cliente.application.support.ClienteTestData.VERSION_PERSISTIDA;
import static com.devsu.bank.cliente.application.support.ClienteTestData.joseLemaPersistido;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cliente.application.event.ClienteEliminadoPayload;
import com.devsu.bank.cliente.application.event.ClienteOutboxEvents;
import com.devsu.bank.cliente.application.support.UseCaseHarness;
import com.devsu.bank.cliente.domain.exception.ClienteNoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeleteClienteUseCaseTest {

  private UseCaseHarness harness;
  private DeleteClienteUseCase useCase;

  @BeforeEach
  void setUp() {
    harness = new UseCaseHarness();
    useCase = new DeleteClienteUseCase(harness.transaction(), harness.repository, harness.events);
  }

  @Test
  void should_remove_the_client_when_it_exists() {
    harness.repository.guardarExistente(joseLemaPersistido());

    useCase.execute(CLIENTE_ID);

    assertThat(harness.repository.findById(CLIENTE_ID)).isEmpty();
  }

  @Test
  void should_write_a_ClienteEliminado_tombstone_newer_than_the_last_state() {
    harness.repository.guardarExistente(joseLemaPersistido());

    useCase.execute(CLIENTE_ID);

    assertThat(harness.outbox.todosDentroDeTransaccion()).isTrue();
    assertThat(harness.outbox.unicoEvento().body().eventName())
        .isEqualTo(ClienteOutboxEvents.CLIENTE_ELIMINADO);
    assertThat(harness.outbox.unicoEvento().body().payload())
        .isEqualTo(
            new ClienteEliminadoPayload(CLIENTE_ID.value().toString(), VERSION_PERSISTIDA + 1));
  }

  @Test
  void should_fail_with_not_found_and_delete_nothing_when_the_client_does_not_exist() {
    assertThatThrownBy(() -> useCase.execute(CLIENTE_ID))
        .isInstanceOf(ClienteNoEncontradoException.class);
    assertThat(harness.repository.eliminados()).isEmpty();
    assertThat(harness.outbox.mensajes()).isEmpty();
  }
}
