package com.devsu.bank.cuenta.domain.exception;

import com.devsu.bank.cuenta.domain.model.ClienteId;

public class ClienteNoEncontradoException extends DomainException {

  public ClienteNoEncontradoException(ClienteId clienteId) {
    super("No se conoce el cliente " + clienteId.value());
  }
}
