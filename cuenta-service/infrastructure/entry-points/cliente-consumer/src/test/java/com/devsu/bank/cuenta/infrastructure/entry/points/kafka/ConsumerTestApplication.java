package com.devsu.bank.cuenta.infrastructure.entry.points.kafka;

import com.devsu.bank.cuenta.application.command.ReceivedClienteEvent;
import com.devsu.bank.cuenta.application.port.inbound.ProjectClienteEventPort;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
class ConsumerTestApplication {

  @Bean
  RecordingProjector recordingProjector() {
    return new RecordingProjector();
  }

  /** Fake inbound port: records calls and can be told how to fail. */
  static final class RecordingProjector implements ProjectClienteEventPort {

    final List<ReceivedClienteEvent> aplicados = new CopyOnWriteArrayList<>();
    final AtomicInteger llamadas = new AtomicInteger();
    volatile Consumer<ReceivedClienteEvent> comportamiento = e -> {};

    @Override
    public void execute(ReceivedClienteEvent event) {
      llamadas.incrementAndGet();
      comportamiento.accept(event);
      aplicados.add(event);
    }

    void reiniciar(Consumer<ReceivedClienteEvent> nuevo) {
      aplicados.clear();
      llamadas.set(0);
      comportamiento = nuevo;
    }
  }
}
