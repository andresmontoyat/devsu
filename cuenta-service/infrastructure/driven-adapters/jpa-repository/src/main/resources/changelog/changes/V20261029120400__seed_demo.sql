--liquibase formatted sql

-- changeset andres:20261029120400-01 context:demo
-- comment: Projection of the three demo clients of cliente-service (same cliente_id values) and the
-- comment: accounts of use case 2. Use case 3 (585545) and the movements are run through the API.
INSERT INTO cliente_proyeccion (cliente_id, nombre, activo, eliminado, version)
VALUES ('0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e01', 'Jose Lema', TRUE, FALSE, 0),
       ('0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e02', 'Marianela Montalvo', TRUE, FALSE, 0),
       ('0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e03', 'Juan Osorio', TRUE, FALSE, 0);

INSERT INTO cuenta (numero_cuenta, tipo_cuenta, saldo_inicial, saldo_disponible, estado, cliente_id)
VALUES ('478758', 'AHORROS', 2000.00, 2000.00, TRUE, '0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e01'),
       ('225487', 'CORRIENTE', 100.00, 100.00, TRUE, '0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e02'),
       ('495878', 'AHORROS', 0.00, 0.00, TRUE, '0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e03'),
       ('496825', 'AHORROS', 540.00, 540.00, TRUE, '0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e02');
-- rollback DELETE FROM cuenta WHERE numero_cuenta IN ('478758', '225487', '495878', '496825');
-- rollback DELETE FROM cliente_proyeccion WHERE nombre IN ('Jose Lema', 'Marianela Montalvo', 'Juan Osorio');
