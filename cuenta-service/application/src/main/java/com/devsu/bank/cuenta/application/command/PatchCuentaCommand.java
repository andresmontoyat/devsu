package com.devsu.bank.cuenta.application.command;

/** Partial update (JSON Merge Patch): null = unchanged. Balances are never patchable. */
public record PatchCuentaCommand(String numeroCuenta, String tipoCuenta, Boolean estado) {}
