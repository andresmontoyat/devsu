package com.devsu.bank.cliente.application.support;

import com.devsu.bank.cliente.application.command.CreateClienteCommand;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ClienteId;
import com.devsu.bank.cliente.domain.model.DatosPersona;
import com.devsu.bank.cliente.domain.model.Genero;
import com.devsu.bank.cliente.domain.model.Identificacion;
import com.devsu.bank.cliente.domain.model.Telefono;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

public final class ClienteTestData {

  public static final Clock CLOCK =
      Clock.fixed(Instant.parse("2022-02-10T15:00:00Z"), ZoneOffset.UTC);
  public static final ClienteId CLIENTE_ID = ClienteId.of("7f1d3c9a-2b4e-4f60-9a1b-3c5d7e9f1a2b");
  public static final String IDENTIFICACION = "1712345678";
  public static final String CONTRASENA = "1234";
  public static final long VERSION_PERSISTIDA = 4L;

  private ClienteTestData() {}

  public static CreateClienteCommand crearJoseLema() {
    return new CreateClienteCommand(
        "Jose Lema",
        "MASCULINO",
        35,
        IDENTIFICACION,
        "Otavalo sn y principal",
        "098254785",
        CONTRASENA,
        true);
  }

  public static DatosPersona datosJoseLema() {
    return new DatosPersona(
        "Jose Lema",
        Genero.MASCULINO,
        35,
        new Identificacion(IDENTIFICACION),
        "Otavalo sn y principal",
        new Telefono("098254785"));
  }

  public static Cliente joseLemaPersistido() {
    return Cliente.builder()
        .clienteId(CLIENTE_ID)
        .datos(datosJoseLema())
        .contrasena(RecordingPasswordHasher.hashDe(CONTRASENA))
        .activo(true)
        .version(VERSION_PERSISTIDA)
        .build();
  }
}
