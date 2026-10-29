package com.devsu.bank.cuenta.application.command;

import java.util.UUID;

/**
 * A client lifecycle event as received from cliente-service. {@code messageId} is the outbox row id
 * (dedup key); {@code nombre} and {@code estado} are absent on ClienteEliminado.
 */
public record ReceivedClienteEvent(
    UUID messageId,
    String eventName,
    String clienteId,
    String nombre,
    Boolean estado,
    long version) {

  public static final String CLIENTE_CREADO = "ClienteCreado";
  public static final String CLIENTE_ACTUALIZADO = "ClienteActualizado";
  public static final String CLIENTE_ELIMINADO = "ClienteEliminado";
}
