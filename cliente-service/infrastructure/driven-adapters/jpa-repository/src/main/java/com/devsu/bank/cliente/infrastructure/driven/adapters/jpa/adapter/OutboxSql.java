package com.devsu.bank.cliente.infrastructure.driven.adapters.jpa.adapter;

final class OutboxSql {

  /**
   * Claims due rows of one origin. SKIP LOCKED gives concurrent dispatchers disjoint rows; the NOT
   * EXISTS keeps per-key order: a row waits while any earlier row of its key is unpublished.
   * IN_FLIGHT rows older than the TTL belong to a crashed dispatcher and are taken over.
   */
  static final String CLAIM =
      """
      WITH candidatos AS (
          SELECT o.id
          FROM outbox_message o
          WHERE o.origin = :origin
            AND ((o.status = 'PENDING'
                  AND (o.next_attempt_at IS NULL OR o.next_attempt_at <= now()))
                 OR (o.status = 'IN_FLIGHT'
                     AND o.claimed_at <= now() - make_interval(secs => :ttlSeconds)))
            AND NOT EXISTS (
                SELECT 1
                FROM outbox_message previo
                WHERE previo.message_key = o.message_key
                  AND previo.seq < o.seq
                  AND previo.status <> 'PUBLISHED')
          ORDER BY o.seq
          LIMIT :batchSize
          FOR UPDATE SKIP LOCKED
      )
      UPDATE outbox_message m
      SET status = 'IN_FLIGHT', claimed_at = now(), claimed_by = :claimedBy
      FROM candidatos c
      WHERE m.id = c.id
      RETURNING m.id, m.seq, m.origin, m.topic, m.message_key, m.payload, m.status, m.attempts,
                m.error, m.occurred_at, m.next_attempt_at, m.published_at, m.traceparent
      """;

  static final String MARK_SEND_STARTED =
      "UPDATE outbox_message SET send_started_at = now() WHERE id = :id";

  static final String UPDATE =
      """
      UPDATE outbox_message
      SET status = :status, attempts = :attempts, error = :error,
          next_attempt_at = :nextAttemptAt, published_at = :publishedAt,
          claimed_at = NULL, claimed_by = NULL
      WHERE id = :id AND status = 'IN_FLIGHT'
      """;

  private OutboxSql() {}
}
