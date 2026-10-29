package com.devsu.bank.cliente.domain.exception;

public class DatoInvalidoException extends DomainException {

  private final String campo;

  public DatoInvalidoException(String campo, String detalle) {
    super(campo + ": " + detalle);
    this.campo = campo;
  }

  public String getCampo() {
    return campo;
  }
}
