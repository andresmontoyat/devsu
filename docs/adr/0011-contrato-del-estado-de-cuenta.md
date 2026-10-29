---
status: accepted
date: 2026-10-29
decision-makers: [Carlos Andres Montoya Tobon]
---

# 0011. Contrato del estado de cuenta: GET /reportes con rango y filas del ejemplo

## Context and Problem Statement

F4 fija el endpoint `/reportes?fecha=rango fechas & cliente` y da un JSON de ejemplo con claves en español y
con espacios ("Numero Cuenta", "Saldo Disponible"). Falta definir cómo se expresa el rango y qué significa
"Saldo Inicial" en cada fila.

## Considered Options

- `fecha=inicio,fin` (o un solo día); filas con las claves exactas del ejemplo, más un bloque de cuentas con saldos
- `fechaInicio` y `fechaFin` como parámetros separados, con claves en camelCase
- Devolver solo la lista de filas del ejemplo

## Decision Outcome

Chosen option: **`fecha=inicio,fin` y filas con las claves del ejemplo**, porque respeta literalmente el
contrato del enunciado y le suma lo que pide en texto: cuentas asociadas con sus saldos (más créditos y débitos
del rango) y detalle de movimientos.

"Saldo Inicial" de cada fila es el saldo de la cuenta justo antes de ese movimiento (`saldo_anterior`). Las
fechas se muestran como `d/M/yyyy` en la zona `America/Guayaquil` y el rango máximo es de 366 días.

### Consequences

- **Good** — la fila coincide con el ejemplo del enunciado (100 → 600 → 700).
- **Good** — no hay que recalcular nada: el saldo anterior ya está guardado en cada movimiento (ADR-0008).
- **Bad** — conviven dos estilos de clave en la misma respuesta (camelCase en `cuentas`, claves del ejemplo en
  `movimientos`).

### Confirmation

- `EstadoCuentaTest`, `GenerateEstadoCuentaUseCaseTest` y `RangoFechasTest`.
- `karate/reportes/reportes.feature`, la E2E (caso 5, filas exactas) y la colección Postman.
