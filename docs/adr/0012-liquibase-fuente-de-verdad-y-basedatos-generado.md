---
status: accepted
date: 2026-10-29
decision-makers: [Carlos Andres Montoya Tobon]
---

# 0012. Liquibase como fuente de verdad del esquema; BaseDatos.sql generado

## Context and Problem Statement

El enunciado pide un entregable `BaseDatos.sql`, mientras que cada servicio aplica su esquema con migraciones
al arrancar. Dos fuentes del esquema mantenidas a mano terminan desalineadas.

## Considered Options

- Liquibase como única fuente; `BaseDatos.sql` generado con `pg_dump` y verificado en el build
- `BaseDatos.sql` escrito a mano además de los changelogs
- Solo `BaseDatos.sql`, aplicado por los servicios al arrancar

## Decision Outcome

Chosen option: **Liquibase manda y `BaseDatos.sql` se genera**. `./gradlew generateBaseDatos` aplica los
changelogs de ambos servicios en un PostgreSQL 17.11 desechable y vuelca `cliente_db` y `cuenta_db`.

Las migraciones corren con un usuario de DDL (`*_owner`) y la aplicación usa uno que solo tiene DML (`*_app`).

### Consequences

- **Good** — un solo origen; el script entregable no puede quedar desactualizado sin que falle el build.
- **Good** — la aplicación no puede alterar su propio esquema.
- **Bad** — regenerar el script exige Docker.
- **Neutral** — los changesets confirmados son inmutables; un cambio es un changeset nuevo.

### Confirmation

- `BaseDatosSqlIT`: el archivo coincide con lo que generan los changelogs y carga en un servidor vacío con los datos de ejemplo.
- `DemoSeedIT` en ambos servicios.
