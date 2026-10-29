---
status: accepted
date: 2026-10-29
decision-makers: [Carlos Andres Montoya Tobon]
---

# 0009. Idempotency-Key en Valkey, con fail-closed y circuit breaker

## Context and Problem Statement

`POST /movimientos` no es idempotente: si el cliente reintenta tras un *timeout*, el débito se puede aplicar dos
veces. Los clientes necesitan una forma segura de reintentar.

## Decision Drivers

- Un reintento con la misma clave devuelve el movimiento original y no crea otro.
- La misma clave con otro cuerpo se rechaza.
- Si el almacén de claves falla, nunca se arriesga un doble débito.
- Latencia acotada: el chequeo está en el camino de la petición.

## Considered Options

- Header `Idempotency-Key` opcional, guardado en Valkey con `SET NX PX`
- Tabla de claves en PostgreSQL
- Sin idempotencia: el cliente consulta antes de reintentar

## Decision Outcome

Chosen option: **Valkey con `SET NX PX`**. La clave guarda `IN_PROGRESS|huella` mientras se procesa y
`DONE|huella|movimientoId` al terminar: el TTL es de 24 h, y de 1 min en curso por si la petición muere a medias.

Ante una falla de Valkey la API responde `503` con `Retry-After` (*fail-closed*). Un *circuit breaker* de
Resilience4j corta las llamadas mientras Valkey no responde.

### Consequences

- **Good** — reintentos seguros y respuesta idéntica a la original.
- **Good** — TTL nativo: las claves vencidas no requieren limpieza.
- **Bad** — una dependencia más en el camino de escritura; si Valkey cae, fallan los movimientos que traen clave
  (los que no la traen siguen funcionando).
- **Neutral** — Valkey habla el protocolo Redis: Spring Data Redis con Lettuce lo usa sin cambios.

### Confirmation

- `ValkeyIdempotencyStoreAdapterIT`: reserva, *replay*, huella distinta, Valkey inaccesible y circuito abierto.
- `IdempotentRegisterMovimientoUseCaseTest` y el escenario de `Idempotency-Key` de la colección Postman.
