package com.devsu.bank.cuenta.application.port.outbound;

@FunctionalInterface
public interface TransactionCallbackPort<R> {

  R call();
}
