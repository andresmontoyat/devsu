# Arquitectura

Dos microservicios con arquitectura hexagonal (puertos y adaptadores), cada uno dueño de su base de
datos, comunicados de forma asíncrona por Kafka mediante un *transactional outbox*.

| Servicio | Agregados | Puerto API | Puerto actuator | Base de datos |
|----------|-----------|-----------|-----------------|---------------|
| `cliente-service` | Persona, Cliente | 8081 | 9081 | `cliente_db` |
| `cuenta-service` | Cuenta, Movimiento (+ proyección de Cliente) | 8082 | 9082 | `cuenta_db` |

## 1. Contexto y contenedores

```mermaid
flowchart LR
    usuario([Cliente HTTP<br/>Postman / Karate])

    subgraph cs[cliente-service]
        capi[API REST /api/clientes]
        cout[(outbox_message)]
        cdb[(cliente_db)]
        disp[Dispatcher del outbox]
    end

    subgraph qs[cuenta-service]
        qapi[API REST<br/>/api/cuentas · /api/movimientos · /api/reportes]
        cons[Consumidor Kafka]
        qdb[(cuenta_db<br/>cuentas · movimientos · cliente_proyeccion)]
    end

    kafka{{Kafka<br/>clientes.cliente.events}}
    dlt{{clientes.cliente.events.dlt}}
    valkey[(Valkey<br/>Idempotency-Key)]
    lgtm[otel-lgtm<br/>Grafana · Tempo · Prometheus]

    usuario --> capi
    usuario --> qapi
    capi -- misma transacción --> cdb
    capi -- misma transacción --> cout
    disp -- reclama filas --> cout
    disp -- publica --> kafka
    kafka --> cons
    cons -- proyección idempotente --> qdb
    cons -. venenosos .-> dlt
    qapi --> qdb
    qapi --> valkey
    capi -. OTLP .-> lgtm
    qapi -. OTLP .-> lgtm
```

**Por qué asíncrono.** `cuenta-service` necesita saber si un cliente existe y está activo para abrirle una
cuenta, y su nombre para el estado de cuenta. En vez de consultarlo por HTTP en cada petición (acoplamiento
temporal: si `cliente-service` cae, nadie abre cuentas), mantiene una **proyección local** alimentada por
eventos. Cada servicio sigue funcionando aunque el otro esté caído; la proyección se pone al día al volver.

## 2. Capas hexagonales

Cada servicio es un build Gradle multi-módulo; las dependencias apuntan siempre hacia el dominio y
ArchUnit lo verifica en cada build.

```mermaid
flowchart TB
    subgraph entry[infrastructure/entry-points]
        rest[rest-controller]
        sched[scheduler-task<br/>solo cliente]
        consumer[cliente-consumer<br/>solo cuenta]
    end
    subgraph app[application]
        uc[casos de uso]
        pin[puertos de entrada]
        pout[puertos de salida]
    end
    dom[domain<br/>agregados · value objects · reglas]
    subgraph driven[infrastructure/driven-adapters]
        jpa[jpa-repository]
        kprod[kafka-producer<br/>solo cliente]
        bcrypt[bcrypt-adapter<br/>solo cliente]
        redis[redis-cache<br/>solo cuenta]
        metrics[metrics-publisher]
    end
    boot[bootstrap<br/>cableado Spring]

    rest --> pin
    sched --> pin
    consumer --> pin
    pin --> uc
    uc --> dom
    uc --> pout
    jpa -. implementa .-> pout
    kprod -. implementa .-> pout
    bcrypt -. implementa .-> pout
    redis -. implementa .-> pout
    metrics -. decora .-> pin
    boot --> entry
    boot --> driven
```

| Capa | Contiene | No contiene |
|------|----------|-------------|
| `domain` | `Cliente` (hereda de `Persona`), `Cuenta`, `Movimiento`, value objects (`Monto`, `NumeroCuenta`, `Identificacion`…), excepciones de negocio | Ninguna dependencia de framework |
| `application` | Casos de uso, comandos, puertos, contrato del outbox | Anotaciones de Spring, `@Transactional` (la transacción es un puerto: `TransactionPort`) |
| `infrastructure` | JPA, Kafka, Valkey, BCrypt, REST, métricas | Reglas de negocio |
| `bootstrap` | `@Configuration`, `application.yml`, tests de integración, Karate y ArchUnit | Lógica |

Las métricas (`usecase.operation`, `.calls`, `.errors`) se añaden con **decoradores** sobre los puertos de
entrada (cadena de responsabilidad armada en `UseCaseConfig`), así los casos de uso no saben de Micrometer.

## 3. Publicación de eventos: transactional outbox

```mermaid
sequenceDiagram
    autonumber
    participant API as ClienteController
    participant UC as CreateClienteUseCase
    participant DB as cliente_db
    participant D as OutboxDispatchScheduler
    participant K as Kafka

    API->>UC: crear(comando)
    UC->>DB: BEGIN
    UC->>DB: INSERT persona, cliente
    UC->>DB: INSERT outbox_message (PENDING)
    UC->>DB: COMMIT
    Note over UC,DB: el evento existe si y solo si el cliente existe
    DB-->>D: AFTER_COMMIT (y sondeo cada 5 s como red de seguridad)
    D->>DB: reclamar lote — FOR UPDATE SKIP LOCKED, orden por clave
    D->>K: send(key = clienteId, headers: outbox-message-id, traceparent)
    K-->>D: ack (acks=all, idempotente)
    D->>DB: marcar PUBLISHED (o reintento con backoff exponencial → FAILED)
```

- **Sin doble escritura:** la fila del outbox se confirma en la misma transacción que el cliente; un fallo de
  Kafka nunca pierde ni inventa eventos.
- **Varias instancias:** el reclamo con `SKIP LOCKED` y un TTL de reclamo permite escalar el dispatcher en
  horizontal sin publicar dos veces el mismo lote; una instancia caída libera sus filas al vencer el TTL.
- **Orden por cliente:** la clave del mensaje es el `clienteId` (misma partición) y un evento no se publica
  mientras uno anterior de la misma clave siga pendiente.

## 4. Consumo idempotente y proyección

```mermaid
sequenceDiagram
    autonumber
    participant K as Kafka
    participant L as ClienteEventListener
    participant UC as ProjectClienteEventUseCase
    participant DB as cuenta_db

    K->>L: ClienteCreado / ClienteActualizado / ClienteEliminado
    L->>UC: proyectar(evento)
    UC->>DB: BEGIN
    UC->>DB: INSERT consumer_processed_event ON CONFLICT DO NOTHING
    alt ya procesado (entrega duplicada)
        UC-->>L: ignorar
    else nuevo
        UC->>DB: upsert cliente_proyeccion si version > actual (las eliminaciones dejan tombstone)
    end
    UC->>DB: COMMIT
    L->>K: ack manual (solo tras el commit)
    Note over L,K: errores transitorios: reintento con backoff exponencial<br/>mensaje inválido o regla de negocio: directo a .dlt
```

Kafka entrega *at-least-once*; la tabla de eventos procesados y la versión del agregado convierten eso en
*effectively-once* y toleran desorden y repeticiones.

## 5. Registro de movimientos (F2, F3)

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente HTTP
    participant API as MovimientoController
    participant I as IdempotentRegisterMovimiento
    participant V as Valkey
    participant UC as RegisterMovimientoUseCase
    participant DB as cuenta_db

    C->>API: POST /movimientos {numeroCuenta, valor} + Idempotency-Key (opcional)
    API->>I: registrar
    opt con Idempotency-Key
        I->>V: SET NX (IN_PROGRESS + huella del cuerpo)
        alt ya completado con la misma huella
            V-->>I: DONE → devuelve el movimiento original (sin nuevo débito)
        end
    end
    I->>UC: ejecutar
    UC->>DB: SELECT cuenta FOR UPDATE (bloqueo pesimista)
    UC->>UC: Cuenta.aplicarMovimiento(valor)
    alt saldo insuficiente
        UC-->>API: SaldoNoDisponibleException
        API-->>C: 422 problem+json · detail "Saldo no disponible"
    else
        UC->>DB: INSERT movimiento (saldo_anterior, valor, saldo) + UPDATE cuenta
        UC-->>API: movimiento
        API-->>C: 201 Created + Location
    end
```

- El signo del valor define el tipo (`DEPOSITO` / `RETIRO`); el saldo nunca queda negativo (regla del
  dominio y `CHECK` en la base).
- Concurrencia: el bloqueo pesimista serializa los movimientos de una misma cuenta; un test con 10 retiros
  concurrentes de 150 sobre 1000 confirma que solo pasan 6 y el saldo queda en 100.
- Cada movimiento guarda `saldo_anterior` y `saldo`: el historial es auditable y el reporte muestra el
  "Saldo Inicial" de cada fila sin recalcular.
- Si Valkey no responde, un *circuit breaker* abre y la API contesta `503` con `Retry-After` en vez de
  arriesgar un doble débito (*fail-closed*).

## 6. Modelo de datos

```mermaid
erDiagram
    persona ||--|| cliente : "hereda (JOINED)"
    persona {
        bigint id PK
        varchar nombre
        varchar genero
        smallint edad
        varchar identificacion UK
        varchar direccion
        varchar telefono
        bigint version
    }
    cliente {
        bigint persona_id PK, FK
        uuid cliente_id UK
        varchar contrasena_hash
        boolean estado
    }
    outbox_message {
        uuid id PK
        bigint seq
        varchar topic
        varchar message_key
        text payload
        varchar status
        int attempts
        varchar traceparent
    }
    cliente_proyeccion ||--o{ cuenta : posee
    cliente_proyeccion {
        uuid cliente_id PK
        varchar nombre
        boolean activo
        boolean eliminado
        bigint version
    }
    cuenta ||--o{ movimiento : registra
    cuenta {
        bigint id PK
        varchar numero_cuenta UK
        varchar tipo_cuenta
        numeric saldo_inicial
        numeric saldo_disponible
        boolean estado
        uuid cliente_id FK
        bigint version
    }
    movimiento {
        bigint id PK
        uuid movimiento_id UK
        varchar numero_cuenta FK
        timestamptz fecha
        varchar tipo_movimiento
        numeric valor
        numeric saldo_anterior
        numeric saldo
    }
    consumer_processed_event {
        uuid message_id PK
        timestamptz processed_at
    }
```

`cliente_db` (arriba: `persona`, `cliente`, `outbox_message`) y `cuenta_db` (el resto) son bases distintas;
no hay claves foráneas entre servicios. El esquema completo está en [`BaseDatos.sql`](../BaseDatos.sql),
generado desde los changelogs de Liquibase.

## 7. Rendimiento, escalabilidad y resiliencia

| Factor | Decisión |
|--------|----------|
| Rendimiento | Índices para cada consulta (`ix_movimiento_cuenta_fecha` para el reporte, `ix_cuenta_cliente`); paginación por offset en todos los listados; reporte limitado a 366 días; `open-in-view` desactivado; pool Hikari acotado. |
| Escalabilidad | Servicios sin estado: se escalan en horizontal detrás de un balanceador. El dispatcher del outbox reclama con `SKIP LOCKED`; el topic tiene 3 particiones por `clienteId`, así hasta 3 consumidores reparten carga sin perder el orden por cliente. Base de datos por servicio. |
| Resiliencia | Outbox con reintentos y backoff; consumidor con reintentos, DLT y ack manual; *circuit breaker* sobre Valkey; timeouts explícitos en Kafka (productor), Redis y Hikari; *graceful shutdown*; probes de liveness/readiness separadas (la base caída saca la instancia del balanceador sin reiniciarla). |
| Observabilidad | Trazas OTLP (Micrometer + OpenTelemetry) con `traceparent` propagado por Kafka; métricas Prometheus/OTLP por caso de uso; `traceId` en logs y en cada `problem+json`. |
| Seguridad | Contraseñas con BCrypt, nunca devueltas ni registradas; roles de base separados (DDL para Liquibase, DML para la app); contenedores sin root; actuator en un puerto no publicado. |
