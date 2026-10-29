package com.devsu.bank.cliente.infrastructure.driven.adapters.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Insert-only mapping: the dispatcher side reads and updates rows with plain SQL. */
@Entity
@Table(name = "outbox_message")
public class OutboxMessageJpaEntity {

  @Id private UUID id;

  @Column(nullable = false)
  private String origin;

  @Column(nullable = false)
  private String topic;

  @Column(name = "message_key")
  private String messageKey;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(nullable = false, length = 16)
  private String status;

  @Column(nullable = false)
  private int attempts;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(length = 55)
  private String traceparent;

  protected OutboxMessageJpaEntity() {}

  public OutboxMessageJpaEntity(UUID id, String origin, String topic) {
    this.id = id;
    this.origin = origin;
    this.topic = topic;
  }

  public UUID getId() {
    return id;
  }

  public void setMessageKey(String messageKey) {
    this.messageKey = messageKey;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setAttempts(int attempts) {
    this.attempts = attempts;
  }

  public void setOccurredAt(Instant occurredAt) {
    this.occurredAt = occurredAt;
  }

  public void setTraceparent(String traceparent) {
    this.traceparent = traceparent;
  }
}
