package com.devsu.bank.cliente.infrastructure.driven.adapters.bcrypt;

import com.devsu.bank.cliente.application.port.outbound.PasswordHasherPort;
import com.devsu.bank.cliente.domain.model.ContrasenaHash;
import com.devsu.bank.cliente.domain.model.ContrasenaPlana;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** BCrypt with an explicit cost: 10 rounds ≈ 70 ms on commodity hardware, a deliberate cost. */
@Component
public class BcryptPasswordHasherAdapter implements PasswordHasherPort {

  static final int STRENGTH = 10;

  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(STRENGTH);

  @Override
  public ContrasenaHash hash(ContrasenaPlana contrasena) {
    return new ContrasenaHash(encoder.encode(contrasena.value()));
  }
}
