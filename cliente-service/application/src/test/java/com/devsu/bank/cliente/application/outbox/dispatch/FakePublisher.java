package com.devsu.bank.cliente.application.outbox.dispatch;

import com.devsu.bank.cliente.application.outbox.OutboxMessage;
import com.devsu.bank.cliente.application.port.outbound.MessagePublisherPort;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

final class FakePublisher implements MessagePublisherPort {

  private final FakeDispatchStore store;
  private final Set<UUID> fallan = new HashSet<>();
  private final List<UUID> publicados = new ArrayList<>();
  private boolean siempreMarcadoAntes = true;

  FakePublisher(FakeDispatchStore store) {
    this.store = store;
  }

  void fallarCon(UUID id) {
    fallan.add(id);
  }

  @Override
  public void publish(OutboxMessage<String> message) {
    siempreMarcadoAntes &= store.envioIniciado(message.id());
    if (fallan.contains(message.id())) {
      throw new IllegalStateException("broker caido");
    }
    publicados.add(message.id());
  }

  List<UUID> publicados() {
    return List.copyOf(publicados);
  }

  boolean siempreMarcadoAntesDePublicar() {
    return siempreMarcadoAntes;
  }
}
