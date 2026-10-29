---
status: accepted
date: 2026-10-29
decision-makers: [Carlos Andres Montoya Tobon]
---

# 0003. Dos servicios con base de datos propia y proyección local de clientes

## Context and Problem Statement

El enunciado separa (Cliente, Persona) de (Cuenta, Movimientos). Cuentas necesita saber si un cliente existe y
está activo para abrirle una cuenta, y su nombre para el estado de cuenta. Falta decidir cómo obtiene esos datos
sin acoplarse al otro servicio.

## Decision Drivers

- Abrir cuentas y generar reportes debe funcionar aunque cliente-service esté caído.
- Ningún servicio lee tablas del otro.
- Cada servicio escala y despliega por separado.

## Considered Options

- Base por servicio y proyección local de clientes alimentada por eventos
- Base por servicio y consulta HTTP síncrona a cliente-service
- Base compartida con esquemas separados

## Decision Outcome

Chosen option: **base por servicio y proyección local (`cliente_proyeccion`)**, porque elimina el acoplamiento
temporal: cuenta-service responde con sus propios datos y se pone al día cuando llegan los eventos.

### Consequences

- **Good** — sin llamadas entre servicios en el camino de la petición.
- **Good** — `cliente_db` y `cuenta_db` tienen usuarios distintos; un servicio no puede conectarse a la base del otro.
- **Bad** — consistencia eventual: un cliente recién creado tarda unos milisegundos en poder abrir cuentas
  (`422 CLIENTE_NO_DISPONIBLE` mientras tanto).
- **Neutral** — la proyección guarda solo lo necesario: id, nombre, activo, eliminado y versión.

### Confirmation

- `ClienteEventoIT` (cuenta-service) y la suite `./gradlew e2eTest`, que espera la proyección antes de abrir cuentas.
- `docker/postgres/01-create-databases.sh`: roles por base y `REVOKE ALL ... FROM PUBLIC`.

## More Information

- Related: ADR-0004 (Kafka), ADR-0006 (consumidor idempotente).
