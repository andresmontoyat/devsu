# Changelog

All notable changes to this project will be documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and versions follow
[Semantic Versioning](https://semver.org/).

## [Unreleased]

## [1.0.2] - 2026-10-29

### Added
- `CHANGELOG.md`.
- README: guía paso a paso para probar la aplicación con `curl` (casos de uso 1–5 y F3) y una referencia de
  comandos de Gradle y Docker.

## [1.0.1] - 2026-10-29

### Added
- ADRs 0001–0014 en `docs/adr/` (formato MADR), con índice enlazado desde el README y `docs/arquitectura.md`.

## [1.0.0] - 2026-10-29

### Added
- `cliente-service`: CRUD de `/clientes` (F1); `Cliente` hereda de `Persona`; contraseñas con BCrypt;
  publicación de eventos `ClienteCreado`, `ClienteActualizado` y `ClienteEliminado` mediante un transactional
  outbox hacia Kafka.
- `cuenta-service`: CRU de `/cuentas` y `/movimientos` (F1); registro de movimientos positivos y negativos con
  saldo actualizado e historial (F2); `422 Saldo no disponible` (F3); estado de cuenta en
  `GET /reportes?fecha=inicio,fin&cliente=` con las claves del JSON de ejemplo (F4).
- Proyección local de clientes alimentada por Kafka, con consumidor idempotente, reintentos y DLT.
- `Idempotency-Key` opcional en `POST /movimientos`, guardada en Valkey con circuit breaker.
- Errores en formato RFC 9457 (`application/problem+json`) con `code` estable y `traceId`.
- Esquemas con Liquibase y datos de ejemplo del enunciado (contexto `demo`); `BaseDatos.sql` generado desde los
  changelogs y verificado por `BaseDatosSqlIT`.
- Pruebas: unitaria de la entidad `Cliente` (F5), integración con Testcontainers (F6), Karate dentro de
  `./gradlew build`, ArchUnit, cobertura JaCoCo por módulo y suite E2E contra docker compose
  (`./gradlew e2eTest`).
- Despliegue en contenedores (F7): Dockerfiles multi-stage sin root y `docker/docker-compose.yaml` con
  PostgreSQL, Kafka, Valkey, Grafana LGTM y ambos servicios.
- Observabilidad: trazas OTLP con `traceparent` propagado por Kafka, métricas por caso de uso y `traceId` en logs.
- Colección Postman con entorno, README, `docs/arquitectura.md` y CI de GitHub Actions (build, test y reporte
  de cobertura).

[Unreleased]: https://github.com/andresmontoyat/devsu/compare/v1.0.2...HEAD
[1.0.2]: https://github.com/andresmontoyat/devsu/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/andresmontoyat/devsu/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/andresmontoyat/devsu/releases/tag/v1.0.0
