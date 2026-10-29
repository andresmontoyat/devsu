package com.devsu.bank.cuenta.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import org.junit.jupiter.api.Test;

class DomainExceptionTest {

  private static final NumeroCuenta NUMERO = new NumeroCuenta("478758");
  private static final ClienteId CLIENTE = ClienteId.of("0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e01");
  private static final MovimientoId MOVIMIENTO =
      MovimientoId.of("5a1d2c3b-4e5f-4a6b-9c7d-8e9f0a1b2c3d");

  @Test
  void should_use_the_exact_message_required_by_the_exercise() {
    assertThat(new SaldoNoDisponibleException()).hasMessage("Saldo no disponible");
  }

  @Test
  void should_name_the_offending_resource() {
    assertThat(new CuentaNoEncontradaException(NUMERO)).hasMessageContaining("478758");
    assertThat(new NumeroCuentaDuplicadoException(NUMERO)).hasMessageContaining("478758");
    assertThat(new CuentaInactivaException(NUMERO)).hasMessageContaining("478758");
    assertThat(new CuentaModificadaConcurrentementeException(NUMERO))
        .hasMessageContaining("478758");
    assertThat(new ClienteNoDisponibleException(CLIENTE))
        .hasMessageContaining(CLIENTE.value().toString());
    assertThat(new MovimientoNoEncontradoException(MOVIMIENTO))
        .hasMessageContaining(MOVIMIENTO.value().toString());
    assertThat(new MovimientoNoModificableException(MOVIMIENTO))
        .isInstanceOf(DomainException.class)
        .hasMessageContaining(MOVIMIENTO.value().toString());
  }
}
