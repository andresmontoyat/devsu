package com.devsu.bank.cliente.application.port.outbound;

@FunctionalInterface
public interface TransactionCallbackPort<R> {

  R call();
}
