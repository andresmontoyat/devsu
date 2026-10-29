package com.devsu.bank.cuenta.domain.exception;

import com.devsu.bank.cuenta.domain.model.ClienteId;

public class ClienteNoDisponibleException extends DomainException {

  public ClienteNoDisponibleException(ClienteId clienteId) {
    super("El cliente " + clienteId.value() + " no existe o esta inactivo");
  }
}
