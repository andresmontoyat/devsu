package com.devsu.bank.cliente.infrastructure.driven.adapters.jpa.adapter;

import com.devsu.bank.cliente.application.outbox.ClaimRequest;
import com.devsu.bank.cliente.application.outbox.OutboxMessage;
import com.devsu.bank.cliente.application.outbox.OutboxMessageWritten;
import com.devsu.bank.cliente.application.outbox.OutboxStatus;
import com.devsu.bank.cliente.application.port.outbound.OutboxDispatchStore;
import com.devsu.bank.cliente.application.port.outbound.OutboxWriter;
import com.devsu.bank.cliente.infrastructure.driven.adapters.jpa.entity.OutboxMessageJpaEntity;
import jakarta.persistence.EntityManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.json.JsonMapper;

/**
 * Both sides of the outbox. Writes join the business transaction through JPA; the dispatcher side
 * uses plain SQL because the claim needs {@code FOR UPDATE SKIP LOCKED} and {@code RETURNING}.
 */
@Component
public class OutboxJpaAdapter implements OutboxWriter, OutboxDispatchStore {

  private final EntityManager entityManager;
  private final JdbcClient jdbc;
  private final JsonMapper jsonMapper;
  private final ApplicationEventPublisher events;

  public OutboxJpaAdapter(
      EntityManager entityManager,
      JdbcClient jdbc,
      JsonMapper jsonMapper,
      ApplicationEventPublisher events) {
    this.entityManager = entityManager;
    this.jdbc = jdbc;
    this.jsonMapper = jsonMapper;
    this.events = events;
  }

  /** Mandatory transaction: a row written in its own transaction would be a dual write. */
  @Override
  public <T> void save(OutboxMessage<T> message) {
    if (!TransactionSynchronizationManager.isActualTransactionActive()) {
      throw new IllegalStateException("Outbox writes must join the business transaction");
    }
    entityManager.persist(toEntity(message));
    events.publishEvent(new OutboxMessageWritten(message.id()));
  }

  @Override
  public List<OutboxMessage<String>> claim(ClaimRequest request) {
    return jdbc
        .sql(OutboxSql.CLAIM)
        .param("origin", request.origin())
        .param("ttlSeconds", request.claimTtl().toSeconds())
        .param("batchSize", request.batchSize())
        .param("claimedBy", request.claimedBy())
        .query(OutboxJpaAdapter::claimedRow)
        .list()
        .stream()
        .sorted(Comparator.comparingLong(ClaimedRow::seq))
        .map(ClaimedRow::message)
        .toList();
  }

  @Override
  public void markSendStarted(UUID id) {
    jdbc.sql(OutboxSql.MARK_SEND_STARTED).param("id", id).update();
  }

  /** Fenced on IN_FLIGHT: a row reclaimed or finished by someone else is left untouched. */
  @Override
  public void update(OutboxMessage<String> message) {
    jdbc.sql(OutboxSql.UPDATE)
        .param("id", message.id())
        .param("status", message.status().name())
        .param("attempts", message.attempts())
        .param("error", message.error())
        .param("nextAttemptAt", utc(message.nextAttemptAt()))
        .param("publishedAt", utc(message.publishedAt()))
        .update();
  }

  private <T> OutboxMessageJpaEntity toEntity(OutboxMessage<T> message) {
    OutboxMessageJpaEntity entity =
        new OutboxMessageJpaEntity(message.id(), message.origin(), message.topic());
    entity.setMessageKey(message.messageKey());
    entity.setPayload(jsonMapper.writeValueAsString(message.payload()));
    entity.setStatus(message.status().name());
    entity.setAttempts(message.attempts());
    entity.setOccurredAt(message.occurredAt());
    entity.setTraceparent(message.traceparent());
    return entity;
  }

  private static ClaimedRow claimedRow(ResultSet rs, int rowNum) throws SQLException {
    OutboxMessage<String> message =
        new OutboxMessage<>(
            rs.getObject("id", UUID.class),
            rs.getString("origin"),
            rs.getString("topic"),
            rs.getString("message_key"),
            rs.getString("payload"),
            OutboxStatus.valueOf(rs.getString("status")),
            rs.getInt("attempts"),
            rs.getString("error"),
            instant(rs, "occurred_at"),
            instant(rs, "next_attempt_at"),
            instant(rs, "published_at"),
            rs.getString("traceparent"));
    return new ClaimedRow(rs.getLong("seq"), message);
  }

  private static Instant instant(ResultSet rs, String column) throws SQLException {
    OffsetDateTime value = rs.getObject(column, OffsetDateTime.class);
    return value == null ? null : value.toInstant();
  }

  private static OffsetDateTime utc(Instant instant) {
    return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
  }

  private record ClaimedRow(long seq, OutboxMessage<String> message) {}
}
