--liquibase formatted sql

-- changeset andres:20261029120000-01
-- comment: Local copy of cliente-service clients, fed by its events. version is the SOURCE version
-- comment: (used to discard stale events), not an optimistic lock of this table.
CREATE TABLE cliente_proyeccion (
    cliente_id UUID         PRIMARY KEY,
    nombre     VARCHAR(100) NOT NULL,
    activo     BOOLEAN      NOT NULL,
    eliminado  BOOLEAN      NOT NULL DEFAULT FALSE,
    version    BIGINT       NOT NULL
);
-- rollback DROP TABLE cliente_proyeccion;
