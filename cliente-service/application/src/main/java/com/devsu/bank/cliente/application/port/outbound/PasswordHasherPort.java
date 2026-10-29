package com.devsu.bank.cliente.application.port.outbound;

import com.devsu.bank.cliente.domain.model.ContrasenaHash;
import com.devsu.bank.cliente.domain.model.ContrasenaPlana;

@FunctionalInterface
public interface PasswordHasherPort {

  ContrasenaHash hash(ContrasenaPlana contrasena);
}
