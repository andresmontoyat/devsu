package com.devsu.bank.cuenta.domain.exception;

import com.devsu.bank.cuenta.domain.model.MovimientoId;

public class MovimientoNoEncontradoException extends DomainException {

  public MovimientoNoEncontradoException(MovimientoId movimientoId) {
    super("No existe el movimiento " + movimientoId.value());
  }
}
