---
status: accepted
date: 2026-10-29
decision-makers: [Carlos Andres Montoya Tobon]
---

# 0007. La transacción es un puerto, no una anotación en el caso de uso

## Context and Problem Statement

Los casos de uso deben ser atómicos (cliente + fila de outbox; movimiento + saldo), pero `application` no puede
depender de Spring (ADR-0002). `@Transactional` en el caso de uso rompería esa regla y ocultaría el límite de
la transacción.

## Considered Options

- Puerto `TransactionPort` implementado con `TransactionTemplate` en el adaptador JPA
- `@Transactional` en los casos de uso
- `@Transactional` en los controladores

## Decision Outcome

Chosen option: **`TransactionPort`**, porque el límite transaccional queda explícito en el código del caso de
uso (`transaction.run(() -> ...)`) y los tests lo verifican con un *fake*, sin Spring.

### Consequences

- **Good** — `application` no depende de Spring y el límite de la transacción se ve en el código.
- **Good** — los tests comprueban que la escritura y el outbox ocurren dentro de la misma transacción.
- **Bad** — un bean `TransactionPort<T>` por tipo de resultado en `PersistenceConfig`.

### Confirmation

- ArchUnit: `applicationShouldNotDependOnInfrastructure` prohíbe `org.springframework..` en `application`.
- `JpaTransactionAdapterIT` (commit y rollback reales) y los *fakes* `FakeTransaction` y `TransactionProbe`.
