# Kotlin Code Style

This document defines concrete conventions for this repository. Where not specified, follow the official Kotlin Coding Conventions and Spring Boot recommendations.

Targets
- Readability first: prefer clarity over brevity.
- Domain purity: domain and use cases are free from framework dependencies.
- Deterministic formatting: enforced by tooling (recommend ktlint + detekt).

Formatting
- Max line length: 120. Break method chains sensibly.
- Indentation: 4 spaces. No tabs.
- Braces: always use braces for control flow.
- Imports: no wildcard imports. Keep sorted.
- Annotations: one per line when parameters present; otherwise inline is fine.

Naming
- Packages: all lowercase, no underscores (already in use: co.edu.udem...).
- Classes/Objects: PascalCase (InvoiceUseCase, InvoiceEntity).
- Functions/Properties: lowerCamelCase (createInvoice).
- Constants: UPPER_SNAKE_CASE in companion object or top-level `const val`.
- DTOs vs Entities suffixes: Request/Response for controllers/GraphQL; Entity for persistence; Mapper for mapping classes; Adapter for adapters; Port for ports.

Null-safety & types
- Avoid platform types. Prefer non-null types; use nullable only when necessary.
- Use data classes for immutable models (Domain, DTOs). Avoid setters; use copy().
- Prefer sealed classes/enums for closed hierarchies (InvoiceStatus, events).
- Prefer Result/Either-like patterns or domain-specific exceptions for failures at boundaries; avoid returning null for error signaling.

Error handling
- Domain: throw domain-specific exceptions or return Result types. Do not use Spring exceptions in domain/application layers.
- Entry points: translate exceptions to HTTP/GraphQL error responses in handlers.
- Outbound adapters: map DB/messaging errors to infrastructure exceptions and rethrow as application/domain-relevant exceptions.

Logging
- Use SLF4J logger per class: `private val log = LoggerFactory.getLogger(Class::class.java)`.
- Log at boundaries. Do not log sensitive data. Use structured messages and include correlation IDs when available.
- Levels: DEBUG for detailed flow; INFO for lifecycle events; WARN for recoverable issues; ERROR for failures that bubble up.

Validation
- Entry points perform request validation (e.g., Bean Validation). Map to domain types only after validation passes.
- Domain invariants are validated inside constructors/factory methods of domain models and use cases.

Collections & immutability
- Prefer immutable collections at boundaries. Use `val` by default; `var` only when necessary.

Coroutines & blocking
- Current project is blocking (JDBC, Kafka). If introducing coroutines, ensure adapters are non-blocking; avoid mixing unless isolated.

Mapping
- Keep mappers pure and in mappers package. No business logic.
- Naming: `XxxMapper` with functions `toDomain`, `toEntity`, `toResponse`, etc.

Tests
- Unit tests suffix: `Test`. Structure: given-when-then.
- Use factories/builders for fixtures to reduce duplication.
- Test public API of use cases and controllers. Mock outbound ports in use case tests.
- For mappers, use simple deterministic tests.

Gradle & modules
- No cross-module leakage of frameworks into domain/application.
- Keep dependencies minimal and explicit in each module build.gradle.kts.

SQL & Migrations (Liquibase)
- Use timestamped filenames with clear descriptions: `VYYYYMMDDHHMMSS__what_changed.sql`.
- Avoid destructive changes without safe migrations; prefer additive migrations.

MyBatis
- Keep SQL in mapper XML or annotation styles consistently; prefer XML if queries grow. Map entity fields explicitly; avoid `*` selects.
- Entity naming mirrors table names in singular (InvoiceEntity -> invoice table).

API (REST/GraphQL)
- DTOs are transport-only. Do not leak domain internals. Keep error schema consistent.

Commit messages
- Use Conventional Commits: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`.
