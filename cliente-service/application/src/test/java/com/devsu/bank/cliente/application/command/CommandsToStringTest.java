package com.devsu.bank.cliente.application.command;

import static com.devsu.bank.cliente.application.support.ClienteTestData.CLIENTE_ID;
import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.bank.cliente.application.support.ClienteTestData;
import org.junit.jupiter.api.Test;

class CommandsToStringTest {

  private static final String SECRETO = "clave-secreta";

  @Test
  void should_not_expose_the_password_when_a_create_command_is_logged() {
    CreateClienteCommand base = ClienteTestData.crearJoseLema();
    CreateClienteCommand command =
        new CreateClienteCommand(
            base.nombre(),
            base.genero(),
            base.edad(),
            base.identificacion(),
            base.direccion(),
            base.telefono(),
            SECRETO,
            true);

    assertThat(command.toString()).doesNotContain(SECRETO);
  }

  @Test
  void should_not_expose_the_password_when_an_update_command_is_logged() {
    UpdateClienteCommand command =
        new UpdateClienteCommand(
            CLIENTE_ID,
            "Jose",
            "MASCULINO",
            30,
            "1712345678",
            "Otavalo",
            "098254785",
            SECRETO,
            true);

    assertThat(command.toString()).doesNotContain(SECRETO);
  }

  @Test
  void should_not_expose_the_password_when_a_patch_command_is_logged() {
    PatchClienteCommand command =
        PatchClienteCommand.builder(CLIENTE_ID).contrasena(SECRETO).build();

    assertThat(command.toString()).doesNotContain(SECRETO);
  }
}
