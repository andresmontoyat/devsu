# Devsu Local Stack (Kafka + Postgres + Services)

This compose file runs both Java Spring Boot services together, sharing a single Kafka cluster. Each service has its own Postgres instance. Kafka UI is included for topic inspection.

## Services
- Kafka: `localhost:9093` (host); internal DNS `kafka:9092` for containers
- Zookeeper: `localhost:2181`
- Kafka UI: http://localhost:8080
- Customers service: http://localhost:8181
- Movements service: http://localhost:8182
- Postgres (customers): `localhost:5433` (db: `devsu_customers`, user: `devsu`, pass: `devsu`)
- Postgres (movements): `localhost:5434` (db: `devsu_movements`, user: `devsu`, pass: `devsu`)

## Prerequisites
- Docker and Docker Compose
- ~8GB RAM available for containers during the first build

## Run
```bash
# From repo root
docker compose up -d --build

# Tail logs for the apps
docker compose logs -f customers-service movements-service
```

Wait for Postgres containers to be healthy and for both services to report `Started` in logs.

## Stop and clean
```bash
docker compose down
# Remove volumes if you want a clean DB
# WARNING: this deletes all data and topics
docker compose down -v
```

## Configuration
Both apps pick defaults from `application.yml` but are overridden via environment variables in the compose:
- `SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092` (containers) and host access on `localhost:9093`
- Customers DB: `jdbc:postgresql://postgres-customers:5432/devsu_customers`
- Movements DB: `jdbc:postgresql://postgres-movements:5432/devsu_movements`

If you prefer to run apps on the host (not in containers), point them to host ports:
- Customers DB URL: `jdbc:postgresql://localhost:5433/devsu_customers`
- Movements DB URL: `jdbc:postgresql://localhost:5434/devsu_movements`
- Kafka bootstrap: `localhost:9093`

## Health and troubleshooting
- Use Kafka UI (`http://localhost:8080`) to verify topics and messages
- If apps cannot reach Kafka from host, ensure `EXTERNAL` listener is mapped to `localhost:9093`
- If DB migrations fail, check application logs and ensure Postgres is healthy (`docker ps` then `docker logs <postgres-container>`)

## Notes
- Ports 5433/5434 are used to avoid collision with any local Postgres on 5432
- Kafka exposes an internal listener (`kafka:9092`) for inter-container communication and an external one (`localhost:9093`) for host tools
