package com.devsu.bank.cuenta.domain.exception;

import com.devsu.bank.cuenta.domain.model.MovimientoId;

public class MovimientoNoModificableException extends DomainException {

  public MovimientoNoModificableException(MovimientoId movimientoId) {
    super(
        "Solo el ultimo movimiento de la cuenta puede corregirse; "
            + movimientoId.value()
            + " no lo es");
  }
}
