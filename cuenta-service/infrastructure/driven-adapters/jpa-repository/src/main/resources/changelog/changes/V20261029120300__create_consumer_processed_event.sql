--liquibase formatted sql

-- changeset andres:20261029120300-01
-- comment: Idempotent consumer (messaging.md §7.3): one row per outbox-message-id already applied.
CREATE TABLE consumer_processed_event (
    message_id   UUID        PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Retention job deletes old rows by date.
CREATE INDEX ix_consumer_processed_event_processed_at ON consumer_processed_event (processed_at);
-- rollback DROP TABLE consumer_processed_event;
