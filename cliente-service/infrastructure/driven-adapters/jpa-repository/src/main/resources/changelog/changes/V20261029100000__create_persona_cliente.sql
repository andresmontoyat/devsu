--liquibase formatted sql

-- changeset andres:20261029100000-01
-- comment: Persona/Cliente with JOINED inheritance: cliente's PK is also its FK to persona.
CREATE TABLE persona (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL,
    genero         VARCHAR(10)  NOT NULL,
    edad           SMALLINT     NOT NULL,
    identificacion VARCHAR(20)  NOT NULL,
    direccion      VARCHAR(200) NOT NULL,
    telefono       VARCHAR(16)  NOT NULL,
    version        BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_persona_identificacion UNIQUE (identificacion),
    CONSTRAINT ck_persona_genero CHECK (genero IN ('MASCULINO', 'FEMENINO', 'OTRO')),
    CONSTRAINT ck_persona_edad CHECK (edad BETWEEN 0 AND 150)
);

CREATE TABLE cliente (
    persona_id      BIGINT       PRIMARY KEY,
    cliente_id      UUID         NOT NULL,
    contrasena_hash VARCHAR(100) NOT NULL,
    estado          BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_cliente_cliente_id UNIQUE (cliente_id),
    CONSTRAINT fk_cliente_persona FOREIGN KEY (persona_id) REFERENCES persona (id) ON DELETE CASCADE
);
-- rollback DROP TABLE cliente;
-- rollback DROP TABLE persona;
