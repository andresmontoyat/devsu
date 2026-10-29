package com.devsu.bank.cliente.application.port.outbound;

/** Transactional boundary owned by the use case; implemented in infrastructure. */
@FunctionalInterface
public interface TransactionPort<R> {

  R run(TransactionCallbackPort<R> callback);
}
