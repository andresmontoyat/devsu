# Changelog

All notable changes to this project will be documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and versions follow
[Semantic Versioning](https://semver.org/).

## [Unreleased]

## [2.0.0] - 2026-10-29

### Added
- Contrato OpenAPI por servicio (`openapi/cliente-service.yaml`, `openapi/cuenta-service.yaml`) con ejemplos
  y todos los errores, publicado en `/api/v3/api-docs` con Swagger UI en `/api/swagger-ui.html`.
- ADR-0015: contrato OpenAPI primero.
- ADR-0016: rango de fechas con `fechaInicio` y `fechaFin`.
- README: sección "Mensajería asíncrona" con los topics (particiones, clave, retención, DLT), el contrato
  de los mensajes (headers, sobre y eventos con su efecto) y cómo verlos; y sección "Patrones aplicados".

### Changed
- **Incompatible:** `GET /movimientos` filtra por fechas con `fechaInicio` y `fechaFin` (`yyyy-MM-dd`, van
  juntas) en lugar de `fecha`.
- **Incompatible:** `GET /reportes` recibe el rango con `fechaInicio` y `fechaFin` (obligatorias) en lugar de
  `fecha=inicio,fin`. ADR-0016 reemplaza a ADR-0011.
- La capa REST se genera desde el contrato con `openapi-generator` 7.22: los controladores implementan las
  interfaces generadas y los DTOs escritos a mano se eliminaron. La API no cambia de comportamiento.
- README: sección "Despliegue con Docker" completa: requisitos, obtención del código, configuración,
  orden de arranque, verificación, servicios con puertos y credenciales, operación, variables de `docker/.env`
  y problemas frecuentes.

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

[Unreleased]: https://github.com/andresmontoyat/devsu/compare/v2.0.0...HEAD
[2.0.0]: https://github.com/andresmontoyat/devsu/compare/v1.0.2...v2.0.0
[1.0.2]: https://github.com/andresmontoyat/devsu/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/andresmontoyat/devsu/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/andresmontoyat/devsu/releases/tag/v1.0.0
