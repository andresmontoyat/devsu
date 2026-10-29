package com.devsu.bank.cuenta.application.port.inbound;

import com.devsu.bank.cuenta.application.command.ReceivedClienteEvent;

@FunctionalInterface
public interface ProjectClienteEventPort {

  void execute(ReceivedClienteEvent event);
}
