# Clean Code for Hexagonal Kotlin Services

Goals
- Keep business logic independent from frameworks and transport/persistence details.
- Small, cohesive units with clear responsibilities. Prefer composition over inheritance.

Principles
- Single Responsibility: each class has one reason to change.
- Open/Closed: extend via new classes/adapters; do not modify domain for infrastructure needs.
- Dependency Inversion: domain and application declare ports; infrastructure implements them.
- Tell, Don’t Ask: encapsulate behavior inside domain objects/use cases.
- YAGNI/KISS: do the simplest thing that works; avoid speculative generalization.
- Immutability: prefer immutable domain models and DTOs.

Layers in this repo
- Domain (pure): entities/value objects, enums, domain services if any. No Spring, no I/O.
- Application (use cases): orchestrate domain logic, define inbound/outbound ports, enforce application rules. No persistence/messaging/web.
- Infrastructure (adapters): implementations of outbound ports (DB, Kafka), and entry points (REST/GraphQL/Kafka consumer). Contains frameworks and I/O.
- Bootstrap: application wiring and configuration to assemble the runtime.

Use cases
- Keep as thin as possible but the single place for orchestration and transactions.
- Depend only on ports defined in application.
- Validate inputs and enforce application rules; domain invariants live in domain.

Ports
- Inbound ports: interfaces representing use case API consumed by entry points.
- Outbound ports: interfaces representing dependencies needed by use cases.
- Suffixes: `UseCasePort` for inbound; `Repository`, `Publisher`, etc., for outbound.

Adapters
- Entry points (REST/GraphQL/Kafka): map transport to inbound ports, handle validation and response mapping, no business logic.
- Driven adapters (JDBC/Kafka producer): implement outbound ports, map domain to technology-specific types, handle technical errors.

Mapping
- Create dedicated mappers for each boundary (REST, GraphQL, JDBC, Kafka). Keep pure functions.
- Do not place mapping logic inside use cases or domain objects.

Transactions
- Start and commit/rollback around use case methods that change state. In Spring, annotate the use case implementation with `@Transactional` (application layer) rather than repositories.

Errors
- Prefer meaningful exceptions or sealed error results in domain/application.
- Centralize exception-to-HTTP/GraphQL error mapping in handlers.

Testing strategy
- Domain: pure unit tests without mocks.
- Use cases: unit tests mocking outbound ports; verify behavior and interactions.
- Adapters: slice/integration tests (e.g., MyBatis with Testcontainers), and contract tests for REST/GraphQL.
- End-to-end (optional): thin happy path flows using docker-compose/Testcontainers.

Code smells to avoid
- Anemic domain where all logic is in controllers or repositories.
- Leaking Spring or persistence annotations into domain/application modules.
- God services with many responsibilities; break down by use case.
- Overuse of static utility; prefer domain services or extension functions scoped appropriately.
