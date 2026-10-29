package com.devsu.bank.cliente.domain.exception;

import com.devsu.bank.cliente.domain.model.ClienteId;

public class ClienteNoEncontradoException extends DomainException {

  public ClienteNoEncontradoException(ClienteId clienteId) {
    super("No existe el cliente " + clienteId.value());
  }
}
