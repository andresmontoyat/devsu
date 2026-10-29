--liquibase formatted sql

-- changeset andres:20261029120200-01
-- comment: Ledger. id gives insertion order ("latest movement"); movimiento_id is the public key.
-- comment: Each row is self-checking: saldo = saldo_anterior + valor, never below zero.
CREATE TABLE movimiento (
    id              BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    movimiento_id   UUID          NOT NULL,
    numero_cuenta   VARCHAR(20)   NOT NULL,
    fecha           TIMESTAMPTZ   NOT NULL,
    tipo_movimiento VARCHAR(10)   NOT NULL,
    valor           NUMERIC(15,2) NOT NULL,
    saldo_anterior  NUMERIC(15,2) NOT NULL,
    saldo           NUMERIC(15,2) NOT NULL,
    CONSTRAINT uk_movimiento_id UNIQUE (movimiento_id),
    CONSTRAINT fk_movimiento_cuenta FOREIGN KEY (numero_cuenta) REFERENCES cuenta (numero_cuenta),
    CONSTRAINT ck_movimiento_tipo CHECK (tipo_movimiento IN ('DEPOSITO', 'RETIRO')),
    CONSTRAINT ck_movimiento_valor CHECK (valor <> 0),
    CONSTRAINT ck_movimiento_saldo CHECK (saldo >= 0 AND saldo = saldo_anterior + valor)
);

-- Statement by date range and "latest movement of the account".
CREATE INDEX ix_movimiento_cuenta_fecha ON movimiento (numero_cuenta, fecha);
CREATE INDEX ix_movimiento_cuenta_id ON movimiento (numero_cuenta, id);
-- rollback DROP TABLE movimiento;
