---
status: accepted
date: 2026-10-29
decision-makers: [Carlos Andres Montoya Tobon]
---

# 0013. Cliente hereda de Persona: clase sellada en el dominio y JOINED en JPA

## Context and Problem Statement

El enunciado exige que Cliente sea una entidad que herede de Persona y que ambas tengan su clave primaria. La
herencia debe existir en el modelo de dominio y en la base sin que JPA dicte el diseño del dominio.

## Considered Options

- `Persona` abstracta y `sealed` en el dominio; en JPA, `InheritanceType.JOINED` (tablas `persona` y `cliente`)
- `SINGLE_TABLE` con discriminador
- Composición: `Cliente` contiene un `Persona`

## Decision Outcome

Chosen option: **`sealed` en el dominio y `JOINED` en JPA**. El modelo cumple la herencia que pide el
enunciado, `sealed` impide subtipos no previstos, y `JOINED` da a cada tabla su PK (`persona.id`,
`cliente.persona_id`) más la clave única de negocio `cliente_id`.

### Consequences

- **Good** — tablas normalizadas, con restricciones en cada nivel (edad, género, identificación única).
- **Good** — el dominio no lleva anotaciones JPA: las entidades JPA son clases aparte con su propio mapeo.
- **Bad** — leer un cliente requiere un *join*; la versión optimista vive en `persona` porque Hibernate la
  exige en la entidad raíz.

### Confirmation

- `ClienteTest` (F5) y `ClienteRepositoryAdapterIT`.
- ArchUnit: `domainShouldBeFrameworkFree` y `jpaShouldStayInPersistenceAdapter`.
