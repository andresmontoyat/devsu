package com.devsu.bank.cliente.application.support;

import com.devsu.bank.cliente.application.port.outbound.PasswordHasherPort;
import com.devsu.bank.cliente.domain.model.ContrasenaHash;
import com.devsu.bank.cliente.domain.model.ContrasenaPlana;

/** Deterministic stand-in for BCrypt that also records whether it ran inside the transaction. */
public final class RecordingPasswordHasher implements PasswordHasherPort {

  private final TransactionProbe probe;
  private boolean llamadoDentroDeTransaccion;

  public RecordingPasswordHasher(TransactionProbe probe) {
    this.probe = probe;
  }

  public static ContrasenaHash hashDe(String plana) {
    return new ContrasenaHash("hash:" + plana);
  }

  @Override
  public ContrasenaHash hash(ContrasenaPlana contrasena) {
    llamadoDentroDeTransaccion |= probe.isActive();
    return hashDe(contrasena.value());
  }

  public boolean fueLlamadoDentroDeTransaccion() {
    return llamadoDentroDeTransaccion;
  }
}
