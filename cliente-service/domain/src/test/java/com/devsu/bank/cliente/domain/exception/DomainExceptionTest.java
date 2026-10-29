package com.devsu.bank.cliente.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.bank.cliente.domain.model.ClienteId;
import com.devsu.bank.cliente.domain.model.Identificacion;
import org.junit.jupiter.api.Test;

class DomainExceptionTest {

  private static final ClienteId CLIENTE_ID = ClienteId.of("7f1d3c9a-2b4e-4f60-9a1b-3c5d7e9f1a2b");

  @Test
  void should_name_the_missing_client_when_not_found() {
    assertThat(new ClienteNoEncontradoException(CLIENTE_ID))
        .isInstanceOf(DomainException.class)
        .hasMessageContaining(CLIENTE_ID.value().toString());
  }

  @Test
  void should_name_the_identification_when_duplicated() {
    assertThat(new IdentificacionDuplicadaException(new Identificacion("1712345678")))
        .isInstanceOf(DomainException.class)
        .hasMessageContaining("1712345678");
  }
}
