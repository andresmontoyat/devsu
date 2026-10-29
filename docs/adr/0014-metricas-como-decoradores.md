---
status: accepted
date: 2026-10-29
decision-makers: [Carlos Andres Montoya Tobon]
---

# 0014. Métricas de casos de uso como decoradores encadenados

## Context and Problem Statement

Cada caso de uso necesita latencia, llamadas y errores. Instrumentar dentro del caso de uso mezcla
observabilidad con reglas de negocio y obliga a depender de Micrometer en `application` (ADR-0002).

## Considered Options

- Decoradores sobre los puertos de entrada (cadena de responsabilidad armada en `UseCaseConfig`)
- Llamadas a Micrometer dentro de cada caso de uso
- AOP con `@Timed` y aspectos

## Decision Outcome

Chosen option: **decoradores encadenados**. Cada `*MetricsAdapter` implementa el mismo puerto que decora,
publica `usecase.operation`, `usecase.operation.calls` y `usecase.operation.errors`, y se encadena con
`Chains.build` como bean `@Primary`.

La idempotencia de movimientos usa el mismo mecanismo: decora el registro de movimientos.

### Consequences

- **Good** — los casos de uso siguen sin depender de framework; cada métrica se prueba aislada.
- **Good** — el orden de la cadena es explícito y se ve en un solo archivo.
- **Bad** — un adaptador de métricas por puerto: más clases que con un aspecto genérico.

### Confirmation

- `MetricsAdaptersTest` en `metrics-publisher` de ambos servicios.
- ArchUnit: `adaptersShouldNotDependOnUseCases`.
