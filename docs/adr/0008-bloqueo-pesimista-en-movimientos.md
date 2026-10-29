---
status: accepted
date: 2026-10-29
decision-makers: [Carlos Andres Montoya Tobon]
---

# 0008. Bloqueo pesimista por cuenta al registrar movimientos

## Context and Problem Statement

Dos retiros simultáneos sobre la misma cuenta pueden leer el mismo saldo y dejarlo negativo (F2, F3). Hay que
serializar los movimientos de una cuenta sin frenar los de cuentas distintas.

## Decision Drivers

- El saldo nunca queda negativo, ni siquiera bajo concurrencia.
- Sin reintentos visibles para el cliente en el caso normal.
- Que la base de datos sea la última barrera.

## Considered Options

- `SELECT ... FOR UPDATE` (`PESSIMISTIC_WRITE`) sobre la fila de la cuenta
- Bloqueo optimista con `@Version` y reintentos
- Saldo calculado como suma de movimientos, sin columna de saldo

## Decision Outcome

Chosen option: **bloqueo pesimista por cuenta**, porque en una cuenta con mucha concurrencia el bloqueo optimista
convierte la contención en reintentos y errores `409`. El bloqueo afecta solo a una fila y dura una
transacción corta.

`@Version` se mantiene para las ediciones de la cuenta (PUT/PATCH).

### Consequences

- **Good** — 10 retiros concurrentes de 150 sobre 1000: pasan exactamente 6 y el saldo queda en 100.
- **Good** — `CHECK (saldo_disponible >= 0)` y `CHECK (saldo = saldo_anterior + valor)` refuerzan la regla en la base.
- **Bad** — las escrituras sobre una misma cuenta van en serie; el *throughput* por cuenta lo limita la latencia de la transacción.
- **Neutral** — cada movimiento guarda `saldo_anterior` y `saldo`, así el historial es auditable sin recalcular.

### Confirmation

- `ConcurrentMovimientosIT.should_serialize_concurrent_withdrawals_on_the_same_account`.
- `MovimientoSaldoNoDisponibleIT`.
