---
status: proposed
date: 2026-10-29
decision-makers: [Carlos Andres Montoya Tobon]
---

# 0015. Contrato OpenAPI primero: interfaces y modelos generados, contrato publicado con Swagger UI

## Context and Problem Statement

La API REST se escribió desde el código (controladores y DTOs a mano), sin una especificación OpenAPI.
Los consumidores no tienen un contrato navegable y nada impide que la documentación, si existiera, se
desalinee de la implementación. La guía de diseño de APIs pide *contract-first* (api-design.md §19).

## Decision Drivers

- Un único origen para rutas, parámetros, cuerpos y errores.
- Que un cambio de contrato rompa la compilación si el controlador no lo implementa.
- Documentación navegable (Swagger UI) que sea exactamente el contrato, no una reconstrucción.

## Considered Options

- Contract-first: YAML por servicio, interfaces y modelos generados con `openapi-generator`, contrato
  publicado por springdoc
- YAML escrito a mano, publicado tal cual, sin generar código, con un test de consistencia de rutas
- Code-first: springdoc genera la especificación desde anotaciones `@Operation` en los controladores

## Decision Outcome

Chosen option: **contract-first con `openapi-generator` 7.22**, porque el compilador garantiza que los
controladores implementan cada operación del contrato con sus tipos.

- `rest-controller/src/main/resources/openapi/{servicio}.yaml` es la fuente. El generador `spring`
  (`interfaceOnly`, Bean Validation, sin anotaciones de documentación) produce `ClientesApi`, `CuentasApi`,
  `MovimientosApi`, `ReportesApi` y sus modelos en `build/generated`.
- Los controladores solo implementan esas interfaces; ya no declaran rutas ni DTOs propios.
- springdoc 3 publica **el mismo YAML** en `/api/v3/api-docs` (un `OpenApiCustomizer` reemplaza la
  especificación inferida) y Swagger UI en `/api/swagger-ui.html`, desactivable con
  `SPRINGDOC_SWAGGER_UI_ENABLED=false`.
- Los cuerpos JSON Merge Patch se mapean a `JsonNode` (`schemaMappings`) para seguir rechazando `null` y
  campos desconocidos; las fechas de movimientos se mantienen como `Instant` (`typeMappings`).

### Consequences

- **Good** — contrato, implementación y documentación salen del mismo archivo.
- **Good** — el contrato documenta ejemplos y todos los errores (`problem+json`) de cada operación.
- **Bad** — los modelos generados son POJOs mutables, no records; los mappers usan getters.
- **Bad** — el `produces` de cada operación sale del contrato: una operación cuyo único cuerpo es un error
  debe listar también `application/json`, o un `Accept: application/json` recibe `406` (se encontró con
  Newman en `DELETE /clientes/{id}` y se corrigió en el contrato).
- **Neutral** — los campos de texto con valores cerrados en las peticiones (`genero`, `tipoCuenta`) siguen
  como `string` validado en el dominio, para aceptar mayúsculas y minúsculas como antes; en las respuestas sí
  son `enum`.

### Confirmation

- Compilación: un controlador que no implemente una operación del contrato no compila.
- ArchUnit: `controllersShouldImplementTheContract` (todo `@RestController` implementa una interfaz de
  `..generated.api..`).
- `ClienteApiIT.should_publish_the_openapi_contract_and_the_swagger_ui` y `OpenApiContractIT`.
- Karate, ITs, Newman y la suite E2E sin cambios: el comportamiento de la API se mantuvo.

## More Information

- Related: ADR-0002 (capas), ADR-0010 (errores RFC 9457), ADR-0011 (contrato del estado de cuenta).
