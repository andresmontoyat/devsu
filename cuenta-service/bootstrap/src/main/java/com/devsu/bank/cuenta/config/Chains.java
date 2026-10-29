package com.devsu.bank.cuenta.config;

import com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support.ChainablePort;
import java.util.Comparator;
import java.util.List;
import org.springframework.core.Ordered;

/**
 * Chain of responsibility in front of a port: links run lowest order first and the use case is
 * always the last link.
 */
final class Chains {

  private Chains() {}

  @SuppressWarnings("unchecked")
  static <T> T build(Class<T> port, T core, List<ChainablePort<?>> links) {
    List<ChainablePort<T>> innermostFirst =
        links.stream()
            .filter(port::isInstance)
            .sorted(Comparator.comparingInt(Ordered::getOrder).reversed())
            .map(link -> (ChainablePort<T>) link)
            .toList();
    T next = core;
    for (ChainablePort<T> link : innermostFirst) {
      link.setDelegate(next);
      next = (T) link;
    }
    return next;
  }
}
