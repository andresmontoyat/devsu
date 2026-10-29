package com.devsu.bank.cuenta.application.support;

/** Lets fakes observe whether they were called inside the use case's transaction. */
public final class TransactionProbe {

  private boolean active;
  private int runs;

  void begin() {
    active = true;
    runs++;
  }

  void end() {
    active = false;
  }

  public boolean isActive() {
    return active;
  }

  public int runs() {
    return runs;
  }
}
