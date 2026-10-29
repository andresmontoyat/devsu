---
status: proposed
date: 2026-10-29
decision-makers: [Carlos Andres Montoya Tobon]
---

# 0017. Versionado de la API en la ruta: /api/v1

## Context and Problem Statement

La API se publicó sin versión (`/api/{recurso}`), como la nombra el enunciado. Con ADR-0016 la API ya tuvo su
primer cambio incompatible (rango de fechas) y no había forma de que una versión nueva conviviera con la
anterior. La guía de diseño de APIs pide versionar en la ruta (api-design.md §5).

## Decision Drivers

- La versión debe verse en logs, trazas y en el gateway, y poder enrutarse.
- Una versión mayor nueva debe poder convivir con la anterior durante su deprecación.
- Versionar es responsabilidad del adaptador REST, no del dominio ni de los casos de uso.

## Considered Options

- Versión mayor en la ruta (`/api/v1/...`) para todos los recursos
- `/api/v1/...` más un alias sin versión (`/api/...`) para la URL literal del enunciado
- Sin versión en la ruta; versión solo en `info.version` del contrato
- Versión en el header `Accept` (`application/vnd.devsu.v1+json`)

## Decision Outcome

Chosen option: **versión mayor en la ruta, sin alias**, porque es lo que pide la guía y deja un solo camino
documentado en el contrato.

- `ApiVersionConfig` (en cada `rest-controller`) agrega el prefijo `/v1` a los controladores del adaptador con
  `PathMatchConfigurer.addPathPrefix`; con el *context path* `/api`, las rutas quedan `/api/v1/{recurso}`.
- Los contratos declaran `servers: /api/v1`; sus rutas siguen siendo relativas (`/clientes`).
- `info.version` sigue la versión de la API v1 (`1.0.0`, menores y parches); el tag del repositorio es otra
  cosa. Un cambio incompatible futuro publica `/api/v2` con controladores propios que comparten los casos de uso.
- springdoc (`/api/v3/api-docs`, `/api/swagger-ui.html`) y el actuator no se versionan.

### Consequences

- **Good** — la versión es explícita en cada URL y en el contrato; v2 podrá convivir con v1.
- **Good** — el dominio y los casos de uso no cambian: el prefijo vive en el adaptador.
- **Bad** — las URLs sin versión (`/api/clientes`, la forma literal del enunciado) dejan de responder (`404`).
  Postman, Karate, la suite E2E y el README usan `/api/v1`.

### Confirmation

- `ClienteApiIT` y `OpenApiContractIT`: el contrato publicado declara `servers[0].url = /api/v1`.
- Tests de controlador, Karate, E2E y Newman llaman a `/api/v1/...`; una ruta sin versión no tiene handler.

## More Information

- Related: ADR-0015 (contrato OpenAPI primero), ADR-0016 (rango de fechas).
