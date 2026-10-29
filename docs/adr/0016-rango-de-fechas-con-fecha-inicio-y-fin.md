---
status: accepted
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
- Un mismo criterio de rango en los dos endpoints que filtran por fecha.
- Swagger UI no debe mostrar dos formas de enviar lo mismo.

## Considered Options

- `fechaInicio` y `fechaFin` (`date`) en los dos endpoints, reemplazando a `fecha`
- `fechaInicio` y `fechaFin`, conservando `fecha` como alias deprecado en `/reportes`
- Mantener `fecha=inicio,fin` (ADR-0011)

## Decision Outcome

Chosen option: **`fechaInicio` y `fechaFin` en los dos endpoints, sin alias**, porque tipa el contrato y deja
una sola forma de pedir el rango. El alias se probó y se descartó: Swagger UI mostraba tres campos de fecha
para un mismo rango.

- `GET /movimientos?fechaInicio=&fechaFin=`: opcionales, pero van juntas; sin ninguna no se filtra por fecha.
- `GET /reportes?fechaInicio=&fechaFin=&cliente=`: los tres obligatorios.
- Rango invertido o de más de 366 días, o una sola fecha en `/movimientos`: `400 RANGO_FECHAS_INVALIDO`.
  Falta de un parámetro obligatorio o fecha que no es ISO-8601: `400 PARAMETRO_INVALIDO`. Los días se cuentan en
  `America/Guayaquil`.
- Los parámetros de fecha no llevan `example` en el contrato: Swagger UI los rellenaría con una fecha fija sin
  datos; quedan vacíos con el formato indicado.

### Consequences

- **Good** — Swagger UI muestra dos campos `date` y el formato; los clientes generados reciben `LocalDate`.
- **Bad** — cambio incompatible: `fecha` deja de funcionar en los dos endpoints, incluida la URL literal del
  enunciado (`/reportes?fecha=…`), que ahora responde `400`. Se registra en el CHANGELOG y la versión mayor sube.
- **Neutral** — `RangoFechas` del dominio no cambia; la regla de `/movimientos` (opcionales pero juntas) vive en
  el adaptador REST (`RangosDeFechas`).

### Confirmation

- `MovimientoControllerTest` y `ReporteControllerTest`: rango válido, una sola fecha, rango invertido, formato
  no ISO, parámetros faltantes y `fecha` rechazada.
- Karate `movimientos.feature` (filtro por rango) y `reportes.feature`.
- E2E `./gradlew e2eTest` y la colección Postman, que usan la forma nueva.

## More Information

- Supersedes: ADR-0011.
- Related: ADR-0015 (contrato OpenAPI primero).
