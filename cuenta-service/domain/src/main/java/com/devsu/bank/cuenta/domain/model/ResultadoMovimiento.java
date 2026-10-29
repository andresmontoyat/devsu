package com.devsu.bank.cuenta.domain.model;

/** The account after a movement plus the ledger entry that explains the change. */
public record ResultadoMovimiento(Cuenta cuenta, Movimiento movimiento) {}
