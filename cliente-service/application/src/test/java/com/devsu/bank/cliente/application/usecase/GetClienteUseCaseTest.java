package com.devsu.bank.cliente.application.usecase;

import static com.devsu.bank.cliente.application.support.ClienteTestData.CLIENTE_ID;
import static com.devsu.bank.cliente.application.support.ClienteTestData.joseLemaPersistido;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cliente.application.support.InMemoryClienteRepository;
import com.devsu.bank.cliente.domain.exception.ClienteNoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GetClienteUseCaseTest {

  private InMemoryClienteRepository repository;
  private GetClienteUseCase useCase;

  @BeforeEach
  void setUp() {
    repository = new InMemoryClienteRepository();
    useCase = new GetClienteUseCase(repository);
  }

  @Test
  void should_return_the_client_when_it_exists() {
    repository.guardarExistente(joseLemaPersistido());

    assertThat(useCase.execute(CLIENTE_ID)).isEqualTo(joseLemaPersistido());
  }

  @Test
  void should_fail_with_not_found_when_the_client_does_not_exist() {
    assertThatThrownBy(() -> useCase.execute(CLIENTE_ID))
        .isInstanceOf(ClienteNoEncontradoException.class);
  }
}
