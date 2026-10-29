package com.devsu.bank.cliente.infrastructure.driven.adapters.kafka;

public class MessagePublishException extends RuntimeException {

  public MessagePublishException(String message, Throwable cause) {
    super(message, cause);
  }
}
