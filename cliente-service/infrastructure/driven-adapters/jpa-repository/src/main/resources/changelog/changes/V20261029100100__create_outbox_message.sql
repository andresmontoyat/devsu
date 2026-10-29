--liquibase formatted sql

-- changeset andres:20261029100100-01
-- comment: Transactional outbox, same shape as the codehunters SDK 9.x table (messaging.md §4.2).
CREATE TABLE outbox_message (
    id              UUID         PRIMARY KEY,
    seq             BIGINT       GENERATED ALWAYS AS IDENTITY NOT NULL UNIQUE,
    origin          VARCHAR(255) NOT NULL,
    routing_key     VARCHAR(255),
    topic           VARCHAR(255) NOT NULL,
    message_key     VARCHAR(255),
    payload         TEXT         NOT NULL,
    status          VARCHAR(16)  NOT NULL,
    attempts        INT          NOT NULL DEFAULT 0,
    error           VARCHAR(512),
    occurred_at     TIMESTAMPTZ  NOT NULL,
    next_attempt_at TIMESTAMPTZ,
    published_at    TIMESTAMPTZ,
    claimed_at      TIMESTAMPTZ,
    claimed_by      VARCHAR(255),
    send_started_at TIMESTAMPTZ,
    traceparent     VARCHAR(55)
);

-- Claim scans due rows of one origin in insert order.
CREATE INDEX ix_outbox_message_claim ON outbox_message (origin, status, seq);
-- Per-key ordering check: "is there an earlier unpublished row with my key?"
CREATE INDEX ix_outbox_message_key_seq ON outbox_message (message_key, seq);
-- rollback DROP TABLE outbox_message;
