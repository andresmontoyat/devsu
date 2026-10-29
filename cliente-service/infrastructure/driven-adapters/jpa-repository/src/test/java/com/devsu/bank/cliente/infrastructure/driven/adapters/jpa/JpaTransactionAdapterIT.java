package com.devsu.bank.cliente.infrastructure.driven.adapters.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cliente.infrastructure.driven.adapters.jpa.adapter.JpaTransactionAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class JpaTransactionAdapterIT extends AbstractJpaIT {

  @Test
  void should_run_the_callback_inside_a_transaction_and_return_its_result() {
    JpaTransactionAdapter<Boolean> transaction = new JpaTransactionAdapter<>(transactionTemplate);

    assertThat(transaction.run(TransactionSynchronizationManager::isActualTransactionActive))
        .isTrue();
  }

  @Test
  void should_roll_back_every_write_when_the_callback_fails() {
    JpaTransactionAdapter<Void> transaction = new JpaTransactionAdapter<>(transactionTemplate);

    assertThatThrownBy(
            () ->
                transaction.run(
                    () -> {
                      jdbc.sql(
                              "INSERT INTO persona (nombre, genero, edad, identificacion, direccion,"
                                  + " telefono) VALUES ('X', 'OTRO', 30, '1234567', 'D', '0999999')")
                          .update();
                      throw new IllegalStateException("fallo de negocio");
                    }))
        .isInstanceOf(IllegalStateException.class);
    assertThat(jdbc.sql("SELECT COUNT(*) FROM persona").query(Long.class).single()).isZero();
  }
}
