package com.devsu.bank.cliente.infrastructure.driven.adapters.bcrypt;

import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.bank.cliente.domain.model.ContrasenaHash;
import com.devsu.bank.cliente.domain.model.ContrasenaPlana;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BcryptPasswordHasherAdapterTest {

  private static final ContrasenaPlana CONTRASENA = new ContrasenaPlana("1234");

  private final BcryptPasswordHasherAdapter adapter = new BcryptPasswordHasherAdapter();

  @Test
  void should_produce_a_bcrypt_hash_that_matches_the_password() {
    ContrasenaHash hash = adapter.hash(CONTRASENA);

    assertThat(hash.value()).startsWith("$2a$10$").doesNotContain("1234");
    assertThat(new BCryptPasswordEncoder().matches("1234", hash.value())).isTrue();
  }

  @Test
  void should_salt_every_hash() {
    assertThat(adapter.hash(CONTRASENA)).isNotEqualTo(adapter.hash(CONTRASENA));
  }
}
