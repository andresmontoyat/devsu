---
status: proposed
date: 2026-10-29
decision-makers: [Carlos Andres Montoya Tobon]
supersedes: 0011
---

# 0016. Rango de fechas con fechaInicio y fechaFin en reportes y movimientos

## Context and Problem Statement

ADR-0011 expresó el rango de `/reportes` como un único parámetro `fecha=inicio,fin`, tomado literal del
enunciado, y `GET /movimientos` copió la misma forma. Un solo `string` con dos fechas separadas por coma no
se puede tipar en el contrato OpenAPI: los clientes no ven que son fechas, ni su formato, ni cuál es el
inicio. Se pide expresar el rango con dos parámetros de tipo `date`.

Lo que ADR-0011 decidió sobre el contenido del reporte no cambia: cuentas con saldos y totales del rango,
filas con las claves exactas del JSON de ejemplo y "Saldo Inicial" = saldo antes de cada movimiento.

## Decision Drivers

- El contrato debe describir cada fecha como `format: date` (`yyyy-MM-dd`).
- El enunciado fija `/reportes?fecha=rango fechas & cliente`: quien evalúe con esa forma no debe recibir `400`.
- Un mismo criterio de rango en los dos endpoints que filtran por fecha.

## Considered Options

- `fechaInicio` y `fechaFin` (`date`); en `/reportes` se mantiene `fecha` como alias deprecado
- `fechaInicio` y `fechaFin` reemplazando por completo a `fecha`, también en `/reportes`
- Mantener `fecha=inicio,fin` (ADR-0011)

## Decision Outcome

Chosen option: **`fechaInicio` y `fechaFin`, con `fecha` deprecada solo en `/reportes`**, porque tipa el
contrato sin romper la forma que el enunciado fija para el reporte.

- `GET /movimientos?fechaInicio=&fechaFin=`: opcionales, pero van juntas; sin ninguna no se filtra por fecha.
- `GET /reportes?fechaInicio=&fechaFin=&cliente=`: el rango es obligatorio. `fecha=inicio,fin` (o un día) se
  sigue aceptando y se marca `deprecated: true` en el contrato; enviar las dos formas a la vez es un error.
- Una sola fecha, un rango invertido o de más de 366 días: `400 RANGO_FECHAS_INVALIDO`. Una fecha que no es
  ISO-8601: `400 PARAMETRO_INVALIDO`. Los días se cuentan en `America/Guayaquil`.

### Consequences

- **Good** — Swagger UI muestra selectores de fecha y el formato; los clientes generados reciben `LocalDate`.
- **Good** — la URL del enunciado sigue funcionando.
- **Bad** — cambio incompatible en `GET /movimientos`: `fecha` deja de filtrar (se ignora como parámetro
  desconocido). Se registra en el CHANGELOG.
- **Bad** — `/reportes` acepta dos formas hasta retirar el alias.
- **Neutral** — `RangoFechas` del dominio no cambia; la regla de combinación vive en el adaptador REST
  (`RangosDeFechas`).

### Confirmation

- `MovimientoControllerTest` y `ReporteControllerTest`: rango válido, una sola fecha, rango invertido, formato
  no ISO, mezcla de formas y alias deprecado.
- Karate `movimientos.feature` (filtro por rango) y `reportes.feature` (forma nueva y alias).
- E2E `./gradlew e2eTest` y la colección Postman, que usan la forma nueva.

## More Information

- Supersedes: ADR-0011.
- Related: ADR-0015 (contrato OpenAPI primero).
