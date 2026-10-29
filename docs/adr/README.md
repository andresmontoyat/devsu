# Registro de decisiones de arquitectura

Formato [MADR](https://adr.github.io/madr/). Un ADR aceptado no se edita: para cambiar una decisión se escribe
uno nuevo con `supersedes` y el anterior pasa a `superseded`.

| ADR | Decisión | Estado |
|-----|----------|--------|
| [0001](0001-registrar-decisiones-de-arquitectura.md) | Registrar las decisiones de arquitectura como ADRs en el repositorio | accepted |
| [0002](0002-arquitectura-hexagonal-multimodulo.md) | Arquitectura hexagonal con un módulo Gradle por capa y adaptador | accepted |
| [0003](0003-dos-servicios-base-de-datos-por-servicio.md) | Dos servicios con base de datos propia y proyección local de clientes | accepted |
| [0004](0004-comunicacion-asincrona-con-kafka.md) | Comunicación asíncrona entre servicios con Kafka | accepted |
| [0005](0005-transactional-outbox-propio.md) | Publicar eventos con un transactional outbox propio | accepted |
| [0006](0006-consumidor-idempotente-y-dlt.md) | Consumidor idempotente con tabla de mensajes procesados, versión y DLT | accepted |
| [0007](0007-transaccion-como-puerto.md) | La transacción es un puerto, no una anotación en el caso de uso | accepted |
| [0008](0008-bloqueo-pesimista-en-movimientos.md) | Bloqueo pesimista por cuenta al registrar movimientos | accepted |
| [0009](0009-idempotency-key-en-valkey.md) | Idempotency-Key en Valkey, con fail-closed y circuit breaker | accepted |
| [0010](0010-errores-rfc-9457.md) | Errores como Problem Details (RFC 9457) con un código estable | accepted |
| [0011](0011-contrato-del-estado-de-cuenta.md) | Contrato del estado de cuenta: GET /reportes con rango y filas del ejemplo | superseded por 0016 |
| [0012](0012-liquibase-fuente-de-verdad-y-basedatos-generado.md) | Liquibase como fuente de verdad del esquema; BaseDatos.sql generado | accepted |
| [0013](0013-cliente-hereda-de-persona.md) | Cliente hereda de Persona: clase sellada en el dominio y JOINED en JPA | accepted |
| [0014](0014-metricas-como-decoradores.md) | Métricas de casos de uso como decoradores encadenados | accepted |
| [0015](0015-contrato-openapi-primero.md) | Contrato OpenAPI primero: interfaces y modelos generados, contrato publicado con Swagger UI | accepted |
| [0016](0016-rango-de-fechas-con-fecha-inicio-y-fin.md) | Rango de fechas con fechaInicio y fechaFin en reportes y movimientos | accepted |
