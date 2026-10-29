package com.devsu.bank.cliente.domain.exception;

import com.devsu.bank.cliente.domain.model.Identificacion;

public class IdentificacionDuplicadaException extends DomainException {

  public IdentificacionDuplicadaException(Identificacion identificacion) {
    super("Ya existe un cliente con la identificacion " + identificacion.value());
  }
}
