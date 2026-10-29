package com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.bank.cuenta.domain.model.ClienteProyeccion;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.adapter.ProcessedEventRepositoryAdapter;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProyeccionYEventosIT extends AbstractJpaIT {

  @Autowired private ProcessedEventRepositoryAdapter procesados;

  @Test
  void should_store_and_update_the_client_projection() {
    ClienteProyeccion actual = clientes.findById(MARIANELA).orElseThrow();

    enTransaccion(() -> clientes.save(actual.actualizar("Marianela M.", false, 4)));

    assertThat(clientes.findById(MARIANELA))
        .contains(new ClienteProyeccion(MARIANELA, "Marianela M.", false, false, 4));
  }

  @Test
  void should_register_a_message_only_the_first_time() {
    UUID mensaje = UUID.randomUUID();

    boolean primero = enTransaccion(() -> procesados.registrarSiNuevo(mensaje));
    boolean segundo = enTransaccion(() -> procesados.registrarSiNuevo(mensaje));

    assertThat(primero).isTrue();
    assertThat(segundo).isFalse();
  }

  @Test
  void should_forget_the_dedup_row_when_the_processing_transaction_rolls_back() {
    UUID mensaje = UUID.randomUUID();

    transactionTemplate.executeWithoutResult(
        status -> {
          procesados.registrarSiNuevo(mensaje);
          status.setRollbackOnly();
        });

    assertThat(enTransaccion(() -> procesados.registrarSiNuevo(mensaje))).isTrue();
  }
}
