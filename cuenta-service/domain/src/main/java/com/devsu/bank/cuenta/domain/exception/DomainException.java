package com.devsu.bank.cuenta.domain.exception;

public abstract class DomainException extends RuntimeException {

  protected DomainException(String message) {
    super(message);
  }
}
