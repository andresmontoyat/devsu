---
status: accepted
date: 2026-10-29
decision-makers: [Carlos Andres Montoya Tobon]
---

# 0005. Publicar eventos con un transactional outbox propio

## Context and Problem Statement

Guardar un cliente y publicar su evento son dos escrituras en sistemas distintos. Publicar directamente desde el
caso de uso puede perder el evento (commit sin publicación) o inventarlo (publicación sin commit).

## Decision Drivers

- Cero eventos perdidos o fantasma ante caídas de Kafka o de la aplicación.
- Varias instancias del servicio sin publicar dos veces el mismo evento.
- Orden por cliente preservado aunque haya reintentos.
- El evaluador debe poder compilar el proyecto sin dependencias privadas.

## Considered Options

- Outbox propio: tabla `outbox_message` en la misma transacción y un dispatcher que reclama filas
- Publicar directamente a Kafka después del commit
- Outbox con CDC (Debezium)
- SDK interno de outbox (contratos idénticos, artefacto privado)

## Decision Outcome

Chosen option: **outbox propio que replica los contratos del SDK interno** (`OutboxWriter`,
`OutboxDispatchStore`, `MessagePublisherPort`, `EventMessage`), porque da la garantía transaccional sin
infraestructura extra y sin artefactos que el evaluador no puede descargar.

El dispatcher corre al confirmarse la transacción (`@TransactionalEventListener(AFTER_COMMIT)`) y, como red de
seguridad, por sondeo. Reclama con `FOR UPDATE SKIP LOCKED` y un TTL de reclamo, publica con `acks=all` e
idempotencia, y reintenta con backoff exponencial hasta `FAILED`.

### Consequences

- **Good** — el evento existe si y solo si el cambio se confirmó.
- **Good** — escalable en horizontal: `SKIP LOCKED` reparte filas entre instancias.
- **Bad** — entrega *at-least-once*: el consumidor debe ser idempotente (ADR-0006).
- **Bad** — código propio que mantener en lugar de una librería.
- **Neutral** — un evento `FAILED` bloquea los posteriores del mismo cliente hasta reencolarlo, a propósito,
  para no romper el orden.

### Confirmation

- `OutboxJpaAdapterIT` (reclamo en orden, retención por clave, reclamo tras caída, reintentos) y `OutboxDispatchIT` (de punta a punta contra Kafka).
- ArchUnit: `useCasesShouldNotInjectDispatcherPorts` y `noDomainPublisherPorts`.

## More Information

- Related: ADR-0004, ADR-0007.
