---
status: accepted
date: 2026-10-29
decision-makers: [Carlos Andres Montoya Tobon]
---

# 0010. Errores como Problem Details (RFC 9457) con un código estable

## Context and Problem Statement

F3 pide alertar "Saldo no disponible" y deja libre la forma de capturar y mostrar el error. Los clientes de la
API necesitan distinguir errores sin depender del texto del mensaje.

## Considered Options

- `application/problem+json` (RFC 9457) con `code` y `traceId`, mapeado en un único `GlobalExceptionHandler`
- Cuerpo propio `{ "error": "...", "mensaje": "..." }`
- Siempre `400` con el texto del error

## Decision Outcome

Chosen option: **Problem Details con `code` estable**. Las excepciones de dominio se traducen a status y código
en un solo lugar (`ErrorCode`).

"Saldo no disponible" es `422 SALDO_NO_DISPONIBLE` y lleva ese texto en `title` y `detail`. Los errores de
validación son `400`, los recursos inexistentes `404`, los conflictos `409`, y la falta de Valkey `503`
con `Retry-After`.

### Consequences

- **Good** — formato estándar; los clientes deciden según `code`.
- **Good** — el `traceId` del error permite encontrar la traza en Tempo.
- **Bad** — los mensajes están en español y sin i18n: es aceptable dentro del alcance de la prueba.

### Confirmation

- `GlobalExceptionHandlerTest`, `MovimientoSaldoNoDisponibleIT` y los escenarios de error de Karate
  (`response.code`) y de la colección Postman.
