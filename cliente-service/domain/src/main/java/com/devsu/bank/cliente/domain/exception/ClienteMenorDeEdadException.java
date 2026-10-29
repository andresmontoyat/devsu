package com.devsu.bank.cliente.domain.exception;

public class ClienteMenorDeEdadException extends DomainException {

  public ClienteMenorDeEdadException(int edad, int edadMinima) {
    super("El cliente debe tener al menos " + edadMinima + " años; edad recibida: " + edad);
  }
}
