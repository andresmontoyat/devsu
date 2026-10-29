package com.devsu.bank.cliente.application.port.inbound;

/** Runs one outbox dispatch batch; returns true when the batch was full and more may be due. */
@FunctionalInterface
public interface SendOutboxMessagesPort {

  boolean execute();
}
