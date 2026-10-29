package com.devsu.bank.cuenta.infrastructure.entry.points.kafka;

import com.devsu.bank.cuenta.application.command.ReceivedClienteEvent;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Tolerant reader (messaging.md §8.5) of the cliente-service envelope: reads only the members this
 * service needs and ignores everything else, so the producer can add fields without a release here.
 * Our own contract copy on purpose — no shared library between bounded contexts.
 */
@Component
public class ClienteEventMapper {

  private final JsonMapper jsonMapper;

  public ClienteEventMapper(JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  public ReceivedClienteEvent toEvent(UUID messageId, String json) {
    if (messageId == null) {
      throw new InvalidClienteEventException("Falta el header outbox-message-id");
    }
    JsonNode body = parse(json).path("body");
    JsonNode payload = body.path("payload");
    return new ReceivedClienteEvent(
        messageId,
        requerido(texto(body.path("eventName")), "eventName"),
        requerido(texto(payload.path("clienteId")), "clienteId"),
        texto(payload.path("nombre")),
        payload.path("estado").isBoolean() ? payload.path("estado").booleanValue() : null,
        version(payload.path("version")));
  }

  private JsonNode parse(String json) {
    try {
      return jsonMapper.readTree(json);
    } catch (JacksonException e) {
      throw new InvalidClienteEventException("El mensaje no es JSON valido", e);
    }
  }

  private static String texto(JsonNode node) {
    return node.isString() ? node.stringValue() : null;
  }

  private static long version(JsonNode node) {
    if (!node.isIntegralNumber()) {
      throw new InvalidClienteEventException("Falta payload.version");
    }
    return node.longValue();
  }

  private static String requerido(String valor, String campo) {
    if (valor == null || valor.isBlank()) {
      throw new InvalidClienteEventException("Falta " + campo);
    }
    return valor;
  }
}
