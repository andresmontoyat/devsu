package com.devsu.bank.cuenta.infrastructure.driven.adapters.metrics.support;

import org.springframework.core.Ordered;

/** A link of a decorator chain in front of a port; bootstrap wires the delegates by order. */
public interface ChainablePort<T> extends Ordered {

  void setDelegate(T delegate);
}
