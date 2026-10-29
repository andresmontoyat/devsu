package com.devsu.bank.cliente.domain.exception;

import com.devsu.bank.cliente.domain.model.ClienteId;

public class ClienteModificadoConcurrentementeException extends DomainException {

  public ClienteModificadoConcurrentementeException(ClienteId clienteId) {
    super("El cliente " + clienteId.value() + " fue modificado por otra operacion; reintente");
  }
}
