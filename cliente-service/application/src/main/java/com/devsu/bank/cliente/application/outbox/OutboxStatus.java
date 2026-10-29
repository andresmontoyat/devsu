package com.devsu.bank.cliente.application.outbox;

public enum OutboxStatus {
  PENDING,
  IN_FLIGHT,
  PUBLISHED,
  FAILED
}
