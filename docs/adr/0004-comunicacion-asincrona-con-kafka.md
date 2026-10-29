---
status: accepted
date: 2026-10-29
decision-makers: [Carlos Andres Montoya Tobon]
---

# 0004. Comunicación asíncrona entre servicios con Kafka

## Context and Problem Statement

El enunciado exige comunicación asíncrona entre los dos microservicios. Los eventos de un mismo cliente
(creado, actualizado, eliminado) deben aplicarse en orden; la proyección debe poder reconstruirse.

## Decision Drivers

- Orden garantizado por cliente.
- Reprocesar el historial para reconstruir la proyección (nuevo consumidor o base vacía).
- Escalar consumidores sin perder ese orden.

## Considered Options

- Kafka (KRaft), topic `clientes.cliente.events` con clave `clienteId`
- RabbitMQ con colas y publisher confirms
- HTTP con webhooks entre servicios

## Decision Outcome

Chosen option: **Kafka con clave `clienteId`**, porque la partición por clave da orden por cliente y paralelismo
entre clientes, y el log retenido permite reconstruir la proyección (`auto-offset-reset: earliest`).

### Consequences

- **Good** — 3 particiones permiten hasta 3 consumidores sin romper el orden por cliente.
- **Good** — `clientes.cliente.events.dlt` aísla mensajes que no se pueden procesar.
- **Bad** — un broker más que operar; en local corre en modo KRaft sin ZooKeeper.
- **Neutral** — los topics son infraestructura: los crea `kafka-init`, no los servicios
  (`auto.create.topics.enable=false`).

### Confirmation

- `OutboxDispatchIT` (`should_publish_ClienteCreado_keyed_by_client_with_envelope_and_headers`).
- `ClienteEventConsumerIT`.
- ArchUnit: `kafkaShouldStayInBrokerAdapter` y `kafkaShouldStayInConsumerAdapter`.

## More Information

- Related: ADR-0005 (outbox), ADR-0006 (consumidor idempotente).
