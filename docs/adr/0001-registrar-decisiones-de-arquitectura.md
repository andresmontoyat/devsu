---
status: accepted
date: 2026-10-29
decision-makers: [Carlos Andres Montoya Tobon]
---

# 0001. Registrar las decisiones de arquitectura como ADRs en el repositorio

## Context and Problem Statement

La solución se defiende en una entrevista técnica y se evalúa leyendo el repositorio. Las razones detrás de
cada elección (outbox, proyección, bloqueos, idempotencia) se pierden si solo viven en conversaciones o en
descripciones de PR.

## Considered Options

- ADRs en formato MADR dentro de `docs/adr/`
- Sección de decisiones solo en el README
- Wiki externa

## Decision Outcome

Chosen option: **ADRs en formato MADR dentro de `docs/adr/`**, porque las decisiones viajan con el código,
se revisan en la misma PR que el cambio y cada una lista cómo se verifica.

### Consequences

- **Good** — cada decisión tiene contexto, alternativas y la prueba que la sostiene.
- **Good** — el README queda corto: resume y enlaza.
- **Bad** — hay que mantener el índice (`README.md`) a mano.
- **Neutral** — un ADR aceptado no se edita; un cambio de decisión es un ADR nuevo que lo reemplaza.

### Confirmation

- Revisión de PR: todo cambio de puerto, adaptador, transacción o contrato público lleva su ADR.
- Índice en `docs/adr/README.md`.
