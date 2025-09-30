# Architecture

This project follows Hexagonal Architecture (Ports and Adapters) with a Gradle multi-module layout. The goal is to isolate the domain and application logic from frameworks and I/O while making adapters replaceable.

## Use Cases in the Application Layer (not the Domain)

### Definition
A Use Case is an application-level service that orchestrates the Domain Model to fulfill a user or system intent (e.g., CreateInvoice, PayInvoice). It coordinates domain objects and ports, applies application policies (transactions, security, idempotency, retries), and translates between the outside world and the domain—without embedding technical details (DB/HTTP/Kafka) or the business rules themselves.

### Why the application layer?

- Separation of concerns: keeps business rules (Entities, Value Objects, Domain Services, Domain Events) pure in the Domain, while workflow/orchestration lives in Application.

- Dependency rule: Application depends inward on Domain; Infrastructure depends on Application. This prevents framework/I/O concerns from leaking into Domain.

- Testability: Use cases are thin, technology-agnostic coordinators—easy to unit test with port fakes/mocks.

- Policy placement: Transactions, authorization, idempotency, saga/compensation are application policies, not domain rules.

### Responsibilities of a Use Case

Validate and normalize scenario inputs (beyond low-level DTO validation).

Load aggregates via outbound ports (e.g., InvoiceRepository), invoke domain behavior, and persist changes.

Publish domain events via an application port (e.g., DomainEventPublisher).

Enforce application policies (transaction boundaries, idempotency keys, security checks).

Expose an inbound port (interface) consumed by entry points (REST, messaging, CLI).

Non-responsibilities (anti-patterns)

❌ Implementing business rules that belong to the Domain (that’s for entities/domain services).

❌ Calling frameworks directly (no JDBC/JPA/Kafka/WebClient here).

❌ Depending on framework annotations or types (keep it POJO/record).


Typical dependency flow:
````
Entry Point (REST/Kafka) -> Inbound Port (Use Case Interface)
Use Case (Application)   -> Domain Model (Entities/VOs/Domain Services)
Use Case                 -> Outbound Ports (Repository/EventPublisher)
Infrastructure           -> Implements Outbound Ports & hosts Entry Points
````
#### Port placement note

Repositories can be defined either in Domain (pure, ubiquitous language) or Application (pragmatic hexagonal). In both variants, Use Cases remain in Application.

#### One-liner for your README

Use cases live in the Application layer: they orchestrate domain behavior, apply application policies, and talk to the outside world through ports—keeping the Domain pure and the Infrastructure replaceable.

Modules in this repository
- domain: pure domain model and enums.
- application: use cases and ports (inbound/outbound). Depends on domain only.
- infrastructure
  - entry-points
    - rest-controller: exposes HTTP endpoints; maps requests/responses.
    - graphql: exposes GraphQL schema and resolvers.
    - kafka-consumer: place for inbound messages if needed (currently stub module).
  - driven-adapters
    - jdbc-repository: MyBatis-based persistence for invoices.
    - kafka-producer: publishes domain events to Kafka.
- bootstrap: Spring Boot application, wiring of beans and configuration.

Dependency rules
- domain: no dependencies on any other module.
- application → domain (only).
- infrastructure (entry/driven) → application and domain as needed.
- bootstrap → application + infrastructure to wire everything.
- Never depend inward on infrastructure from domain/application.

Key packages (examples)
- co.edu.udem.domain: Invoice, value objects, enums.
- co.edu.udem.usecase: InvoiceUseCase and inbound/outbound ports.
- co.edu.udem.infrastructure.entry.points.rest.controller: controllers, requests, responses, mappers.
- co.edu.udem.infrastructure.entry.points.graphql: resolvers, schema, mappers.
- co.edu.udem.infrastructure.jdbc: MyBatis config, repository, entities, mappers.
- co.edu.udem.infrastructure.kafka: publishers, message models.
- co.edu.udem.config (bootstrap): bean configuration for use cases, adapters.

Data flow (example: Create Invoice)
1. REST/GraphQL receives request DTO.
2. Entry point validates and maps DTO to domain model.
3. Entry point calls inbound port (CreateInvoiceUseCasePort).
4. Use case applies business rules; calls outbound ports:
   - InvoiceRepository to persist.
   - InvoicePublisher to emit event.
5. Outbound adapters map domain to tech-specific structures (InvoiceEntity, KafkaMessage), handle I/O.
6. Use case returns domain model; entry point maps it to response DTO.

Transactions
- Place @Transactional at the use case level (application module) for write operations, so a single business operation wraps multiple port calls. Repositories should be thin and not start their own transactions.

Mapping guidelines
- Keep dedicated mappers per boundary:
  - REST/GraphQL mappers: domain ↔ request/response.
  - JDBC mapper: domain ↔ entity and MyBatis mapping.
  - Kafka mapper: domain ↔ Kafka payload.
- No business logic in mappers.

Persistence (MyBatis)
- Entities reflect table columns; use explicit column mapping.
- Keep SQL in mapper XML if queries become complex; avoid `SELECT *`.
- Liquibase manages schema: add a new changeSet per change using timestamped filenames.

Messaging (Kafka)
- Publish domain events after successful transaction where appropriate.
- Model outbound messages in infrastructure.kafka.model.

Error handling strategy
- Domain/application: use domain-specific exceptions or a Result type.
- Entry points: convert exceptions to HTTP status/GraphQL errors (see GlobalExceptionHandler).
- Infrastructure: wrap low-level exceptions and propagate meaningful errors upward.

Testing strategy
- Unit tests in domain and application modules.
- Adapters: slice/integration tests (e.g., using Testcontainers with a real DB/Kafka when needed).
- Contract tests for REST/GraphQL schemas to prevent breaking changes.

Versioning & changes
- Backward compatibility for APIs and messages. Use additive DB migrations.

Checklist before adding new features
- Define/extend inbound and outbound ports as needed in application.
- Implement adapters in the correct infrastructure module.
- Add mappers and DTOs; ensure no domain leakage.
- Add Liquibase changeSets and MyBatis mappings where applicable.
- Write tests at the appropriate layers.
- Wire beans in bootstrap config if auto-detection isn’t sufficient.
