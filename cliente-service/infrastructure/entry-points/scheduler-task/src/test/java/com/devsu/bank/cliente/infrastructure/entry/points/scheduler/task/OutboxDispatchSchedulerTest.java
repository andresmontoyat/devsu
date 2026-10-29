package com.devsu.bank.cliente.infrastructure.entry.points.scheduler.task;

import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.bank.cliente.application.outbox.OutboxMessageWritten;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;

class OutboxDispatchSchedulerTest {

  private static final Executor DIRECTO = Runnable::run;

  @Test
  void should_keep_dispatching_while_batches_come_back_full() {
    Deque<Boolean> respuestas = new ArrayDeque<>(List.of(true, true, false));
    int[] ejecuciones = {0};
    OutboxDispatchScheduler scheduler =
        new OutboxDispatchScheduler(
            () -> {
              ejecuciones[0]++;
              return respuestas.pop();
            },
            DIRECTO);

    scheduler.poll();

    assertThat(ejecuciones[0]).isEqualTo(3);
  }

  @Test
  void should_stop_after_the_maximum_number_of_consecutive_runs() {
    int[] ejecuciones = {0};
    OutboxDispatchScheduler scheduler =
        new OutboxDispatchScheduler(
            () -> {
              ejecuciones[0]++;
              return true;
            },
            DIRECTO);

    scheduler.poll();

    assertThat(ejecuciones[0]).isEqualTo(OutboxDispatchScheduler.MAX_RUNS_PER_DRAIN);
  }

  @Test
  void should_dispatch_right_after_a_commit() {
    int[] ejecuciones = {0};
    OutboxDispatchScheduler scheduler =
        new OutboxDispatchScheduler(
            () -> {
              ejecuciones[0]++;
              return false;
            },
            DIRECTO);

    scheduler.onMessageWritten(new OutboxMessageWritten(UUID.randomUUID()));

    assertThat(ejecuciones[0]).isEqualTo(1);
  }

  @Test
  void should_survive_a_failing_run_and_dispatch_again_later() {
    int[] ejecuciones = {0};
    OutboxDispatchScheduler scheduler =
        new OutboxDispatchScheduler(
            () -> {
              ejecuciones[0]++;
              throw new IllegalStateException("base de datos caida");
            },
            DIRECTO);

    scheduler.poll();
    scheduler.poll();

    assertThat(ejecuciones[0]).isEqualTo(2);
  }

  @Test
  void should_coalesce_requests_while_a_drain_is_already_queued() {
    Deque<Runnable> cola = new ArrayDeque<>();
    int[] ejecuciones = {0};
    OutboxDispatchScheduler scheduler =
        new OutboxDispatchScheduler(
            () -> {
              ejecuciones[0]++;
              return false;
            },
            cola::add);

    scheduler.poll();
    scheduler.poll();
    scheduler.onMessageWritten(new OutboxMessageWritten(UUID.randomUUID()));
    cola.forEach(Runnable::run);

    assertThat(cola).hasSize(1);
    assertThat(ejecuciones[0]).isEqualTo(1);
  }
}
