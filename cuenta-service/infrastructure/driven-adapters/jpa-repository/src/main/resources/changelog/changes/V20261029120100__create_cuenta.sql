--liquibase formatted sql

-- changeset andres:20261029120100-01
-- comment: The CHECK on saldo_disponible is a last line of defence: the domain already refuses
-- comment: overdrafts, the database refuses them too.
CREATE TABLE cuenta (
    id               BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_cuenta    VARCHAR(20)   NOT NULL,
    tipo_cuenta      VARCHAR(10)   NOT NULL,
    saldo_inicial    NUMERIC(15,2) NOT NULL,
    saldo_disponible NUMERIC(15,2) NOT NULL,
    estado           BOOLEAN       NOT NULL DEFAULT TRUE,
    cliente_id       UUID          NOT NULL,
    version          BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT uk_cuenta_numero UNIQUE (numero_cuenta),
    CONSTRAINT ck_cuenta_tipo CHECK (tipo_cuenta IN ('AHORROS', 'CORRIENTE')),
    CONSTRAINT ck_cuenta_saldo_inicial CHECK (saldo_inicial >= 0),
    CONSTRAINT ck_cuenta_saldo_disponible CHECK (saldo_disponible >= 0),
    CONSTRAINT fk_cuenta_cliente FOREIGN KEY (cliente_id) REFERENCES cliente_proyeccion (cliente_id)
);

-- F4 statement and GET /cuentas?cliente= look accounts up by owner.
CREATE INDEX ix_cuenta_cliente ON cuenta (cliente_id);
-- rollback DROP TABLE cuenta;
