Devsu Customer Service

A Spring Boot (Java 21) microservice that manages customers for the Devsu customer domain. It exposes REST endpoints for customer CRUD and status changes, persists data in PostgreSQL, and publishes domain events to Kafka following a hexagonal (ports and adapters) architecture.

Table of contents
- Features
- Tech stack
- Architecture and modules
- Prerequisites
- Quick start
- Configuration
- Build and run
- API reference
- Kafka events
- Testing and coverage
- Postman collection
- Troubleshooting
- Contributing and code style

Features
- Create, read, update, delete customers
- Change customer status (ACTIVE/INACTIVE)
- Password hashing at creation time
- Publish domain events to Kafka on create/delete/status-change
- Health/actuator endpoints enabled

Tech stack
- Language/Runtime: Java 21
- Framework: Spring Boot 3.5.x
- Build: Gradle (Kotlin DSL) with wrapper
- Persistence: PostgreSQL (JPA/Hibernate) + Liquibase migrations
- Messaging: Apache Kafka (with Kafka UI in Docker)
- Mapping/Boilerplate: MapStruct, Lombok
- Testing: JUnit 5, Testcontainers, Karate, JaCoCo

Architecture and modules
Hexagonal architecture with clear boundaries.
- domain: Core domain model (entities, enums)
- application: Use cases and ports (inbound/outbound)
- infrastructure/driven-adapters
  - jpa-repository: JPA repository adapter + Liquibase changelogs
  - kafka-producer: Kafka producer adapter and config
  - security: Crypto service adapter (password hashing)
- infrastructure/entry-points
  - rest-controller: REST controllers, DTOs, and mappers
- bootstrap: Spring Boot application launcher and configuration

Prerequisites
- JDK 21
- Docker and Docker Compose
- Make (optional) and cURL/Postman for testing

Quick start
1) Start local infrastructure (Postgres, Kafka, Kafka UI)
   docker compose up -d

   Services started:
   - Postgres: localhost:5432 (db: devsu_customer, user: devsu, pass: devsu)
   - Kafka: localhost:9093
   - Kafka UI: http://localhost:8080

2) Build the project
   ./gradlew clean build

3) Run the app (choose one)
   - Using Gradle:
     ./gradlew :bootstrap:bootRun

   - As a JAR:
     ./gradlew :bootstrap:bootJar
     java -jar bootstrap/build/libs/devsu-customers.jar

Configuration
Default configuration is in bootstrap/src/main/resources/application.yml.
Key properties (environment variables override these):
- Server
  - SERVER_PORT (default 8181)
  - SERVER_CONTEXT_PATH (default /devsu-customers)
- Spring profile
  - SPRING_PROFILES_ACTIVE (default local)
- Database (when running outside Docker network, prefer localhost)
  - SPRING_DATASOURCE_URL (default jdbc:postgresql://postgres:5432/devsu_customers)
  - SPRING_DATASOURCE_USERNAME (default udem)
  - SPRING_DATASOURCE_PASSWORD (default udem)
  - For local Docker usage, consider setting:
      SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/devsu_customers
      SPRING_DATASOURCE_USERNAME=devsu
      SPRING_DATASOURCE_PASSWORD=devsu
- Liquibase
  - SPRING_LIQUIBASE_ENABLED (default false). Set to true for applying changelogs on startup.
- Kafka
  - SPRING_KAFKA_BOOTSTRAP_SERVERS (default localhost:9093)

Build and run
- Build all modules:
  ./gradlew clean build

- Run unit/integration tests:
  ./gradlew test

- Run application:
  ./gradlew :bootstrap:bootRun

- Package runnable JAR and run:
  ./gradlew :bootstrap:bootJar
  java -jar bootstrap/build/libs/devsu-customers.jar

Docker build and run
- Build image:
  docker build -t devsu-customers .

- Run with Docker (connect to local compose stack):
  # Start infra first (Postgres, Kafka, UI)
  docker compose up -d

  # Run the app container on host network and map port 8181
  docker run --rm \
    --name devsu-customers \
    -p 8181:8181 \
    -e SPRING_PROFILES_ACTIVE=local \
    -e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/devsu_customers \
    -e SPRING_DATASOURCE_USERNAME=devsu \
    -e SPRING_DATASOURCE_PASSWORD=devsu \
    -e SPRING_LIQUIBASE_ENABLED=true \
    -e SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9093 \
    devsu-customers

  # Alternatively, run inside the compose network so service names resolve
  docker network connect $(basename "$PWD")_default devsu-customers 2>/dev/null || true

API reference
Base URL (default): http://localhost:8181/devsu-accounts

- Create customer
  POST /customers
  Content-Type: application/json
  Example request body:
  {
    "name": "John Doe",
    "genre": "M",
    "age": 30,
    "identification": "1234567890",
    "address": "742 Evergreen Terrace",
    "phone": "+57 300 000 0000",
    "clientId": "CUST-001",
    "password": "s3cret"
  }

  Example cURL:
  curl -X POST http://localhost:8181/devsu-accounts/customers \
    -H "Content-Type: application/json" \
    -d '{
      "name":"John Doe",
      "genre":"M",
      "age":30,
      "identification":"1234567890",
      "address":"742 Evergreen Terrace",
      "phone":"+57 300 000 0000",
      "clientId":"CUST-001",
      "password":"s3cret"
    }'

- List customers
  GET /customers

- Get customer by id
  GET /customers/{id}

- Update customer
  PATCH /customers/{id}
  Content-Type: application/json
  Allowed fields: name, genre, age, address, phone
  Example body:
  {
    "address": "New Address",
    "phone": "+57 300 111 2222"
  }

- Delete customer
  DELETE /customers/{id}

- Change customer status
  PATCH /customers/{id}/status/{status}
  Path variable status: ACTIVE or INACTIVE

Typical response (CreatedCustomerResponseDTO):
{
  "id": "c1a3f3c0-...",
  "name": "John Doe",
  "genre": "M",
  "age": 30,
  "identification": "1234567890",
  "address": "742 Evergreen Terrace",
  "phone": "+57 300 000 0000",
  "status": "ACTIVE"
}

Kafka events
Events are published using Spring Kafka (topic names):
- created.customers.events — on customer creation (payload: full customer)
- deleted.customers.events — on customer deletion (payload: { id })
- status.changes.customers.events — on status change (payload: { id, status })

Testing and coverage
- Run tests with coverage reports (JaCoCo):
  ./gradlew test jacocoTestReport

- Aggregate multi-module coverage report:
  ./gradlew codeCoverageReport
  Reports will be generated under each module's build/reports/jacoco and the aggregate task outputs.

Postman collection
- Collection: postman/devsu-accounts.postman_collection.json
- Environment: postman/devsu-accounts.local.postman_environment.json
Import these into Postman and adjust environment variables if needed.

Troubleshooting
- Database connection refused
  - Ensure Docker Compose is up and Postgres is healthy.
  - If running the app on the host, use jdbc:postgresql://localhost:5432/devsu_accounts and credentials devsu/devsu.
  - Consider enabling Liquibase to create schema: SPRING_LIQUIBASE_ENABLED=true

- Kafka not reachable
  - Ensure kafka container is up and listening on 9093.
  - Use Kafka UI at http://localhost:8080 to inspect topics.

License
This project is for educational/demo purposes unless licensed otherwise by the repository owner.
