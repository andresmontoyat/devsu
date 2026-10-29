package com.devsu.bank.cliente.application.outbox;

/**
 * What a use case wants published. Staged builder: topic, key, type and payload are mandatory by
 * construction — {@code OutboxEvent.to(topic).key(id).type(name).payload(p)}.
 */
public record OutboxEvent<T>(
    String topic, String key, String type, T payload, String schemaVersion) {

  public static KeyStage to(String topic) {
    return new KeyStage(requireText(topic, "topic"));
  }

  public OutboxEvent<T> schemaVersion(String version) {
    return new OutboxEvent<>(topic, key, type, payload, requireText(version, "schemaVersion"));
  }

  private static String requireText(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(name + " is required");
    }
    return value;
  }

  public record KeyStage(String topic) {
    public TypeStage key(String key) {
      return new TypeStage(topic, requireText(key, "key"));
    }
  }

  public record TypeStage(String topic, String key) {
    public PayloadStage type(String type) {
      return new PayloadStage(topic, key, requireText(type, "type"));
    }
  }

  public record PayloadStage(String topic, String key, String type) {
    public <T> OutboxEvent<T> payload(T payload) {
      if (payload == null) {
        throw new IllegalArgumentException("payload is required");
      }
      return new OutboxEvent<>(topic, key, type, payload, EventMessage.DEFAULT_SCHEMA_VERSION);
    }
  }
}
