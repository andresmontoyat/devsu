package com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.adapter;

import com.devsu.bank.cuenta.application.port.outbound.TransactionCallbackPort;
import com.devsu.bank.cuenta.application.port.outbound.TransactionPort;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Programmatic transaction: any exception from the callback rolls everything back. A template
 * instead of {@code @Transactional} so one class serves every result type without proxies.
 */
public class JpaTransactionAdapter<R> implements TransactionPort<R> {

  private final TransactionTemplate transactionTemplate;

  public JpaTransactionAdapter(TransactionTemplate transactionTemplate) {
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public R run(TransactionCallbackPort<R> callback) {
    return transactionTemplate.execute(status -> callback.call());
  }
}
