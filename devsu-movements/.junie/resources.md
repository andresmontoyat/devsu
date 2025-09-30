# Resources

Curated references, tools, and templates used by this project. Prefer official docs and stable versions.

- Kotlin
  - Kotlin language: https://kotlinlang.org/docs/home.html
  - Kotlin coding conventions: https://kotlinlang.org/docs/coding-conventions.html
  - Coroutines (if adopted later): https://kotlinlang.org/docs/coroutines-overview.html

- Build & Dependency Management
  - Gradle Kotlin DSL: https://docs.gradle.org/current/userguide/kotlin_dsl.html
  - Version catalogs (libs.versions.toml): https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog

- Spring Boot
  - Docs: https://docs.spring.io/spring-boot/docs/current/reference/html/
  - Configuration properties: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.external-config
  - Validation: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#io.validation

- Architecture
  - Hexagonal Architecture (Ports and Adapters): https://alistair.cockburn.us/hexagonal-architecture/
  - Clean Architecture summary: https://8thlight.com/insights/clean-architecture

- Persistence
  - MyBatis 3: https://mybatis.org/mybatis-3/
  - MyBatis Spring Boot Starter: https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/
  - Liquibase: https://docs.liquibase.com/
  - Database migrations best practices: https://www.red-gate.com/simple-talk/devops/database-devops/liquibase-best-practices/

- Messaging
  - Apache Kafka: https://kafka.apache.org/documentation/
  - Spring for Apache Kafka: https://docs.spring.io/spring-kafka/docs/current/reference/html/

- APIs
  - Spring MVC: https://docs.spring.io/spring-framework/reference/web/webmvc.html
  - Spring for GraphQL: https://docs.spring.io/spring-graphql/docs/current/reference/
  - GraphQL SDL: https://spec.graphql.org/

- Testing
  - JUnit 5: https://junit.org/junit5/docs/current/user-guide/
  - Mockito/Kotest (if used): https://site.mockito.org/ | https://kotest.io/
  - Testcontainers (for future integration tests): https://www.testcontainers.org/

- Code Quality
  - ktlint: https://ktlint.github.io/
  - Detekt: https://detekt.github.io/detekt/
  - Conventional Commits: https://www.conventionalcommits.org/
  - Keep a Changelog: https://keepachangelog.com/en/1.0.0/

- Observability
  - Micrometer: https://micrometer.io/docs
  - Spring Boot Actuator: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html

- Containerization
  - Docker: https://docs.docker.com/
  - Docker Compose: https://docs.docker.com/compose/

- Useful commands (local)
  - Gradle: `./gradlew clean build` | `./gradlew test`
  - Docker Compose: `docker compose up -d` | `docker compose logs -f`

Notes
- Prefer stable LTS versions. Pin versions via Gradle catalogs or properties.
- Cross-check licenses of new dependencies.
