package com.devsu.bank.cuenta.infrastructure.entry.points.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cuenta.application.command.ReceivedClienteEvent;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class ClienteEventMapperTest {

  private static final UUID MENSAJE = UUID.fromString("9e8d7c6b-5a4f-4e3d-8c2b-1a0f9e8d7c6b");
  private static final String CLIENTE = "0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e02";

  private final ClienteEventMapper mapper = new ClienteEventMapper(JsonMapper.builder().build());

  @Test
  void should_read_the_canonical_envelope_and_ignore_unknown_members() {
    String json =
        """
        {"header":{"traceId":"abc","version":"1.0.0","schemaVersion":"1.0.0",
                   "origin":"cliente-service","timestamp":"2022-02-10T15:00:00Z","futuro":1},
         "body":{"aggregate":"%s","eventName":"ClienteActualizado","eventType":"EVENT",
                 "payload":{"clienteId":"%s","nombre":"Marianela Montalvo","estado":false,
                            "version":3,"campoNuevo":"x"}}}
        """
            .formatted(CLIENTE, CLIENTE);

    assertThat(mapper.toEvent(MENSAJE, json))
        .isEqualTo(
            new ReceivedClienteEvent(
                MENSAJE, "ClienteActualizado", CLIENTE, "Marianela Montalvo", false, 3));
  }

  @Test
  void should_read_a_deletion_without_name_or_state() {
    String json =
        """
        {"header":{},"body":{"eventName":"ClienteEliminado",
         "payload":{"clienteId":"%s","version":5}}}
        """
            .formatted(CLIENTE);

    assertThat(mapper.toEvent(MENSAJE, json))
        .isEqualTo(new ReceivedClienteEvent(MENSAJE, "ClienteEliminado", CLIENTE, null, null, 5));
  }

  @Test
  void should_reject_a_message_that_is_not_json() {
    assertThatThrownBy(() -> mapper.toEvent(MENSAJE, "{no-json"))
        .isInstanceOf(InvalidClienteEventException.class);
  }

  @Test
  void should_reject_a_message_without_client_id_or_version() {
    assertThatThrownBy(
            () ->
                mapper.toEvent(
                    MENSAJE, "{\"body\":{\"eventName\":\"ClienteCreado\",\"payload\":{}}}"))
        .isInstanceOf(InvalidClienteEventException.class);
  }

  @Test
  void should_reject_a_message_without_dedup_header() {
    assertThatThrownBy(() -> mapper.toEvent(null, "{}"))
        .isInstanceOf(InvalidClienteEventException.class)
        .hasMessageContaining("outbox-message-id");
  }
}
