package com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.bank.cuenta.application.command.RegisterMovimientoCommand;
import com.devsu.bank.cuenta.application.usecase.RegisterMovimientoUseCase;
import com.devsu.bank.cuenta.domain.exception.SaldoNoDisponibleException;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.adapter.JpaTransactionAdapter;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.adapter.MovimientoRepositoryAdapter;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Ten simultaneous withdrawals of 150 from 1000: with the row lock exactly six succeed and the
 * balance ends at 100. Without it, several would read 1000 and the account would be overdrawn.
 */
class ConcurrentMovimientosIT extends AbstractJpaIT {

  private static final int RETIROS = 10;

  @Autowired private MovimientoRepositoryAdapter movimientos;

  @Test
  void should_serialize_concurrent_withdrawals_on_the_same_account() throws Exception {
    abrir("478758", "1000");
    RegisterMovimientoUseCase useCase =
        new RegisterMovimientoUseCase(
            new JpaTransactionAdapter<>(transactionTemplate),
            cuentas,
            movimientos,
            Clock.systemUTC());
    CountDownLatch salida = new CountDownLatch(1);
    Callable<Boolean> retiro =
        () -> {
          salida.await();
          try {
            useCase.execute(
                new RegisterMovimientoCommand("478758", new BigDecimal("-150"), null, null));
            return true;
          } catch (SaldoNoDisponibleException e) {
            return false;
          }
        };

    List<Boolean> resultados;
    try (ExecutorService pool = Executors.newFixedThreadPool(RETIROS)) {
      List<Future<Boolean>> futuros =
          IntStream.range(0, RETIROS).mapToObj(i -> pool.submit(retiro)).toList();
      salida.countDown();
      resultados = futuros.stream().map(ConcurrentMovimientosIT::esperar).toList();
    }

    assertThat(resultados).filteredOn(ok -> ok).hasSize(6);
    assertThat(cuentas.findByNumero(new NumeroCuenta("478758")).orElseThrow().getSaldoDisponible())
        .isEqualTo(Monto.of("100"));
    assertThat(jdbc.sql("SELECT COUNT(*) FROM movimiento").query(Long.class).single()).isEqualTo(6);
  }

  private static Boolean esperar(Future<Boolean> futuro) {
    try {
      return futuro.get();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
