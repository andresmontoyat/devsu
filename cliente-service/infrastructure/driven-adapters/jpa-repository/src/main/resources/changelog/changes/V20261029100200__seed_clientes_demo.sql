--liquibase formatted sql

-- changeset andres:20261029100200-01 context:demo
-- comment: Sample customers of the exercise. Fixed cliente_id values so cuenta-service can seed
-- comment: its projection with the same keys. Hashes are BCrypt(10) of 1234, 5678 and 1245.
INSERT INTO persona (nombre, genero, edad, identificacion, direccion, telefono)
VALUES ('Jose Lema', 'MASCULINO', 35, '1712345678', 'Otavalo sn y principal', '098254785'),
       ('Marianela Montalvo', 'FEMENINO', 30, '1723456789', 'Amazonas y NNUU', '097548965'),
       ('Juan Osorio', 'MASCULINO', 40, '1734567890', '13 junio y Equinoccial', '098874587');

INSERT INTO cliente (persona_id, cliente_id, contrasena_hash, estado)
SELECT p.id, s.cliente_id, s.contrasena_hash, TRUE
FROM persona p
JOIN (VALUES ('1712345678', CAST('0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e01' AS UUID),
              '$2a$10$80eIfsexxNS0oeLvo8TwB.uLqZBBft2TbJaa7j8aLS.k03D1mmZty'),
             ('1723456789', CAST('0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e02' AS UUID),
              '$2a$10$x3QVHmualh0McM5QkNkKM.ldpHsy3Hd2fZu1yiqg5s83WY.ko4UIS'),
             ('1734567890', CAST('0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e03' AS UUID),
              '$2a$10$24JCNVGQgg.eXLOsH907C.M9igtdR7ctkS/HULs51QbcWNPx/BhiK'))
     AS s (identificacion, cliente_id, contrasena_hash)
  ON s.identificacion = p.identificacion;
-- rollback DELETE FROM persona WHERE identificacion IN ('1712345678', '1723456789', '1734567890');
