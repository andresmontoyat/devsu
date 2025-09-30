package com.devsu.account.usecase.exception;

public class InsufficientInitialBalanceException extends RuntimeException {
    public InsufficientInitialBalanceException(String message) {
        super(message);
    }
}
