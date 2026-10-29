package com.devsu.bank.cuenta.application.support;

import com.devsu.bank.cuenta.application.port.outbound.TransactionCallbackPort;
import com.devsu.bank.cuenta.application.port.outbound.TransactionPort;

public final class FakeTransaction<R> implements TransactionPort<R> {

  private final TransactionProbe probe;

  public FakeTransaction(TransactionProbe probe) {
    this.probe = probe;
  }

  @Override
  public R run(TransactionCallbackPort<R> callback) {
    probe.begin();
    try {
      return callback.call();
    } finally {
      probe.end();
    }
  }
}
