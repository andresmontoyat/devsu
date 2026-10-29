---
status: accepted
date: 2026-10-29
decision-makers: [Carlos Andres Montoya Tobon]
---

# 0006. Consumidor idempotente con tabla de mensajes procesados, versión y DLT

## Context and Problem Statement

Kafka y el outbox entregan *at-least-once*: un evento puede llegar repetido o, tras un reproceso, después de uno
más nuevo. Un mensaje malformado no debe bloquear la partición.

## Decision Drivers

- Aplicar cada evento exactamente una vez sobre la proyección.
- Tolerar duplicados y desorden sin corromper el estado.
- Separar los errores transitorios (reintentar) de los permanentes (apartar).

## Considered Options

- Tabla `consumer_processed_event` + versión del agregado + DLT
- Solo upsert por versión
- Kafka transactions (*exactly-once* de lectura-proceso-escritura)

## Decision Outcome

Chosen option: **tabla de mensajes procesados + versión + DLT**. En una sola transacción se inserta el
`outbox-message-id` (`ON CONFLICT DO NOTHING`) y se aplica el cambio solo si su versión es mayor; el *offset*
se confirma con *ack* manual después del commit.

Los errores transitorios se reintentan con backoff exponencial; los mensajes inválidos y los rechazos de negocio
van directo a `.dlt`.

### Consequences

- **Good** — duplicados y eventos viejos no tienen efecto.
- **Good** — las eliminaciones dejan un *tombstone*, así que un evento atrasado no resucita al cliente.
- **Bad** — la tabla de mensajes procesados crece; tiene índice por `processed_at` para purgarla.
- **Neutral** — Kafka transactions no sirven aquí: la escritura es en PostgreSQL, no en Kafka.

### Confirmation

- `ClienteEventConsumerIT`: `should_apply_a_valid_event_once`,
  `should_retry_a_transient_failure_and_then_apply_the_event`,
  `should_send_a_malformed_message_to_the_dead_letter_topic_without_retrying`.
- `ProjectClienteEventUseCaseTest`.
