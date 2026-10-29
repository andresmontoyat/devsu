# devsu-bank

Prueba técnica Devsu (Java): dos microservicios bancarios — **clientes** y **cuentas/movimientos** —
comunicados de forma asíncrona, desplegables con Docker.

**Stack:** Java 21 · Spring Boot 4.1 · JPA (Hibernate 7) · PostgreSQL 17 · Liquibase · Kafka 4 (KRaft) ·
Valkey 9 · Micrometer + OpenTelemetry · Testcontainers · Karate · ArchUnit · Gradle 9.

```mermaid
flowchart LR
    http([HTTP]) --> cs[cliente-service :8081]
    http --> qs[cuenta-service :8082]
    cs --> cdb[(cliente_db)]
    cs -- outbox --> k{{Kafka}}
    k --> qs
    qs --> qdb[(cuenta_db)]
    qs --> v[(Valkey)]
```

El diseño completo — capas hexagonales, outbox, consumo idempotente, registro de movimientos, modelo de
datos y cómo se cubren rendimiento, escalabilidad y resiliencia — está en
**[docs/arquitectura.md](docs/arquitectura.md)**.

## Despliegue con Docker

Requisitos: Docker 24+ con Compose v2. No hace falta Java para levantar la solución.

```bash
docker compose -f docker/docker-compose.yaml up -d --build --wait
```

Levanta `postgres`, `kafka` (+ `kafka-init`, que crea los topics), `valkey`, `otel-lgtm` y los dos
servicios; `--wait` termina cuando todos están `healthy`. La primera vez compila las imágenes (unos minutos).

| URL | Qué |
|-----|-----|
| http://localhost:8081/api/clientes | cliente-service |
| http://localhost:8082/api/cuentas · `/api/movimientos` · `/api/reportes` | cuenta-service |
| http://localhost:3000 | Grafana (trazas en Tempo, métricas en Prometheus) |
| `localhost:5432` | PostgreSQL (`cliente_db`, `cuenta_db`) |

Al arrancar, Liquibase crea los esquemas y carga los **datos de ejemplo del enunciado** (contexto `demo`):
los clientes Jose Lema, Marianela Montalvo y Juan Osorio y las cuentas del caso 2. Los casos 3 a 5 se
ejecutan por la API (colección Postman o suite E2E).

```bash
docker compose -f docker/docker-compose.yaml logs -f cuenta-service   # logs
docker compose -f docker/docker-compose.yaml down -v                  # parar y borrar datos
```

**Configuración.** Contraseñas y puertos tienen valores por defecto para uso local. Para cambiarlos, copiar
`docker/.env.example` a `docker/.env` (por ejemplo, si el puerto 5432 ya está ocupado:
`POSTGRES_PORT=15432`). `LIQUIBASE_CONTEXTS=` vacío arranca sin datos de ejemplo.

## API

Contrato: `http://{servidor}:{puerto}/api/{recurso}`. Errores en formato
[RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) (`application/problem+json`) con `code` estable y `traceId`.

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` `GET` | `/clientes` | Crear · listar (`?page=&size=`) |
| `GET` `PUT` `PATCH` `DELETE` | `/clientes/{clienteId}` | Obtener · reemplazar · modificar (`application/merge-patch+json`) · eliminar |
| `POST` `GET` | `/cuentas` | Crear · listar (`?cliente=`) |
| `GET` `PUT` `PATCH` | `/cuentas/{numeroCuenta}` | Obtener · reemplazar · modificar |
| `POST` `GET` | `/movimientos` | Registrar (header opcional `Idempotency-Key`) · listar (`?cuenta=&fecha=`) |
| `GET` `PUT` | `/movimientos/{movimientoId}` | Obtener · corregir el último movimiento de la cuenta |
| `GET` | `/reportes?fecha={inicio},{fin}&cliente={clienteId}` | Estado de cuenta (F4); `fecha` admite un día o un rango `yyyy-MM-dd` |

Cuentas y movimientos no se eliminan (CRU, como pide el enunciado): una cuenta se desactiva con
`PATCH {"estado": false}` y un movimiento erróneo se corrige, de modo que el historial sigue siendo auditable.

**Movimiento sin saldo (F3):**

```http
POST /api/movimientos
{"numeroCuenta": "496825", "valor": -100}

HTTP/1.1 422
{"type": "https://api.devsu-bank.example/errores/saldo-no-disponible", "title": "Saldo no disponible",
 "status": 422, "detail": "Saldo no disponible", "code": "SALDO_NO_DISPONIBLE", "traceId": "..."}
```

**Estado de cuenta (F4)** — `GET /api/reportes?fecha=2026-10-29,2026-10-29&cliente=0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e02`.
Cada fila de `movimientos` usa las claves del JSON de ejemplo; `cuentas` añade los saldos y totales del rango:

```json
{
  "cliente": { "clienteId": "0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e02", "nombre": "Marianela Montalvo" },
  "fechaInicio": "2026-10-29",
  "fechaFin": "2026-10-29",
  "cuentas": [
    { "numeroCuenta": "225487", "tipoCuenta": "CORRIENTE", "saldoInicial": 100.0, "saldoDisponible": 700.0,
      "estado": true, "totalCreditos": 600.0, "totalDebitos": 0.0 },
    { "numeroCuenta": "496825", "tipoCuenta": "AHORROS", "saldoInicial": 540.0, "saldoDisponible": 0.0,
      "estado": true, "totalCreditos": 0.0, "totalDebitos": 540.0 }
  ],
  "movimientos": [
    { "Fecha": "29/10/2026", "Cliente": "Marianela Montalvo", "Numero Cuenta": "225487", "Tipo": "Corriente",
      "Saldo Inicial": 100.0, "Estado": true, "Movimiento": 600.0, "Saldo Disponible": 700.0 },
    { "Fecha": "29/10/2026", "Cliente": "Marianela Montalvo", "Numero Cuenta": "496825", "Tipo": "Ahorros",
      "Saldo Inicial": 540.0, "Estado": true, "Movimiento": -540.0, "Saldo Disponible": 0.0 }
  ]
}
```

"Saldo Inicial" de cada fila es el saldo de la cuenta justo antes de ese movimiento.

## Entregables

| Entregable | Dónde |
|------------|-------|
| Script de base de datos | [`BaseDatos.sql`](BaseDatos.sql) — esquema y datos de ejemplo de ambas bases. Se **genera** desde los changelogs de Liquibase (`./gradlew generateBaseDatos`) y `BaseDatosSqlIT` falla si se desalinea. Carga manual: `psql -U postgres -f BaseDatos.sql` |
| Colección Postman | [`postman/devsu-bank.postman_collection.json`](postman/devsu-bank.postman_collection.json) + [entorno](postman/devsu-bank.postman_environment.json) |
| Pruebas Karate | `*/bootstrap/src/test/resources/karate/**` — corren con todos los tests en `./gradlew build` |
| Despliegue en contenedores | `cliente-service/Dockerfile`, `cuenta-service/Dockerfile`, [`docker/docker-compose.yaml`](docker/docker-compose.yaml) |

### Colección Postman

Importar la colección y el entorno en Postman (v9.13+) y ejecutar la carpeta **0. Casos de uso del
enunciado** sobre un stack recién levantado: consulta los datos de los casos 1 y 2, crea la cuenta 585545,
registra los cuatro movimientos, comprueba "Saldo no disponible" y pide el estado de cuenta de Marianela.
Las carpetas 1 a 4 recorren el CRUD de cada recurso con datos propios y se pueden repetir.

Con Newman:

```bash
npx newman run postman/devsu-bank.postman_collection.json -e postman/devsu-bank.postman_environment.json
```

## Funcionalidades

| | Requisito | Implementación |
|-|-----------|----------------|
| F1 | CRUD clientes; CRU cuentas y movimientos | `ClienteController`, `CuentaController`, `MovimientoController` |
| F2 | Movimientos positivos/negativos, saldo actualizado, registro de transacciones | `Cuenta.aplicarMovimiento`; cada `movimiento` guarda saldo anterior y resultante |
| F3 | "Saldo no disponible" | `SaldoNoDisponibleException` → `422` `SALDO_NO_DISPONIBLE` |
| F4 | Reporte por rango de fechas y cliente | `GET /reportes` → `GenerateEstadoCuentaUseCase` |
| F5 | Prueba unitaria de la entidad Cliente | [`ClienteTest`](cliente-service/domain/src/test/java/com/devsu/bank/cliente/domain/model/ClienteTest.java) |
| F6 | Prueba de integración | `ClienteApiIT`, `MovimientoSaldoNoDisponibleIT`, `ClienteEventoIT`, ITs de adaptadores con Testcontainers |
| F7 | Despliegue en contenedores | Dockerfiles multi-stage + docker compose |

## Pruebas

Requisitos: JDK 21 y Docker (Testcontainers levanta PostgreSQL, Kafka y Valkey reales).

```bash
./gradlew build      # unitarias, integración, ArchUnit, Karate, BaseDatos.sql y cobertura de ambos servicios
./gradlew e2eTest    # casos de uso 1-5 contra el stack de docker compose (debe estar levantado)
```

| Nivel | Herramienta | Qué cubre |
|-------|-------------|-----------|
| Dominio | JUnit 6 + AssertJ, sin mocks | Reglas de `Cliente`, `Cuenta`, `Movimiento`, value objects |
| Casos de uso | JUnit con *fakes* de los puertos | Orquestación, transacción, outbox, idempotencia |
| Adaptadores | Testcontainers (PostgreSQL, Kafka, Valkey) | JPA y Liquibase, outbox, consumidor, Idempotency-Key, concurrencia |
| API | Karate (23 escenarios) + `@SpringBootTest` | Contrato HTTP de cada endpoint, errores `problem+json` |
| Arquitectura | ArchUnit | Dependencias entre capas, dominio sin framework |
| E2E | Karate contra docker compose | Casos de uso 1–5 y propagación asíncrona entre servicios |

La cobertura de líneas se exige por módulo en el build: 90 % en `domain` y `application`, 80 % en el
resto (`jacocoTestCoverageVerification`). Actualmente: cliente-service 97.5 %, cuenta-service 96.7 %.
El CI ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)) compila, ejecuta `./gradlew build` y publica
el reporte de cobertura en cada PR.

## Estructura

```
├── cliente-service/          # Persona, Cliente — publica eventos por outbox
│   ├── domain/  application/  bootstrap/
│   └── infrastructure/
│       ├── entry-points/      rest-controller · scheduler-task
│       └── driven-adapters/   jpa-repository · kafka-producer · bcrypt-adapter · metrics-publisher
├── cuenta-service/           # Cuenta, Movimiento, reportes — consume eventos de clientes
│   ├── domain/  application/  bootstrap/
│   └── infrastructure/
│       ├── entry-points/      rest-controller · cliente-consumer
│       └── driven-adapters/   jpa-repository · redis-cache · metrics-publisher
├── database-script/          # genera y verifica BaseDatos.sql
├── e2e-tests/                # suite Karate contra docker compose
├── docker/                   # docker-compose.yaml, init de PostgreSQL, .env.example
├── postman/                  # colección y entorno
├── docs/arquitectura.md
└── BaseDatos.sql
```

Cada servicio es un build Gradle independiente (con su propio wrapper); la raíz es un *composite build*
que los orquesta.

## Decisiones principales

- **Comunicación asíncrona con transactional outbox** en vez de publicar directo a Kafka: el evento se
  confirma en la misma transacción que el cambio, así no hay eventos perdidos ni fantasma.
- **Proyección local de clientes** en cuenta-service: abrir cuentas y generar reportes no depende de que
  cliente-service esté disponible.
- **Consumidor idempotente** (tabla de eventos procesados + versión del agregado) y DLT para mensajes inválidos.
- **Bloqueo pesimista por cuenta** al registrar movimientos y `Idempotency-Key` opcional en Valkey para que
  un reintento del cliente no duplique un débito.
- **Base de datos por servicio**, con un rol de base para migraciones y otro, solo DML, para la aplicación.
- **Liquibase como fuente de verdad del esquema**; `BaseDatos.sql` es un artefacto derivado y verificado.
- **`Cliente` hereda de `Persona`** en el dominio (clase `sealed`) y en JPA (`InheritanceType.JOINED`).
