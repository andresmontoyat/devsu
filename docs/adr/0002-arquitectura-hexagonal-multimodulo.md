---
status: accepted
date: 2026-10-29
decision-makers: [Carlos Andres Montoya Tobon]
---

# 0002. Arquitectura hexagonal con un módulo Gradle por capa y adaptador

## Context and Problem Statement

El enunciado pide aplicar Clean Architecture y buenas prácticas. Las reglas de negocio (saldo, edad mínima,
inmutabilidad de la identificación) deben poder probarse sin base de datos ni framework, y los límites entre
capas deben sostenerse solos, no por disciplina.

## Decision Drivers

- Dominio probable en milisegundos, sin mocks.
- Una dependencia prohibida (dominio → Spring, caso de uso → JPA) debe romper el build.
- Cambiar una tecnología (p. ej. Redis → otra caché) no debe tocar casos de uso.

## Considered Options

- Hexagonal (puertos y adaptadores) con módulos Gradle por capa y por adaptador
- Hexagonal por paquetes dentro de un único módulo
- Capas clásicas controller → service → repository

## Decision Outcome

Chosen option: **hexagonal con módulos Gradle por capa y por adaptador**, porque el compilador impide las
dependencias hacia afuera y ArchUnit cubre lo que el compilador no ve.

Módulos por servicio: `domain`, `application`, `infrastructure/entry-points/*`,
`infrastructure/driven-adapters/*` y `bootstrap`, el único que conoce Spring Boot.

### Consequences

- **Good** — el dominio y los casos de uso no dependen de Spring, Jakarta ni Jackson.
- **Good** — cada adaptador tiene su propio umbral de cobertura y sus pruebas con Testcontainers.
- **Bad** — más módulos y más mapeos (DTO ↔ comando ↔ dominio ↔ entidad JPA).
- **Neutral** — el cableado de beans se concentra en `bootstrap/config`.

### Confirmation

- ArchUnit en `*/bootstrap/src/test/.../ArchitectureTest`: `domainShouldBeFrameworkFree`,
  `domainShouldNotDependOnOuterLayers`, `applicationShouldNotDependOnInfrastructure`,
  `entryPointsShouldNotDependOnDrivenAdapters`, `jpaShouldStayInPersistenceAdapter`.
- JaCoCo por módulo: 90 % en `domain` y `application`, 80 % en el resto.

## More Information

- Related: ADR-0007 (transacción como puerto), ADR-0014 (decoradores de métricas).
