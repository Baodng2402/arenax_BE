# ArenaX Backend

ArenaX backend is a Gradle monorepo for Spring Boot microservices with database-per-service, event-driven boundaries, and centralized service discovery (Eureka).

## Stack

- Java 21
- Spring Boot 4.0.6 / Spring Cloud Gateway 5.0.0 (Netflix 5.0.0)
- Gradle Kotlin DSL with convention plugins under `build-logic/`
- PostgreSQL + Flyway per service
- Spring Data JPA
- RabbitMQ events via transactional outbox (`libs/messaging-foundation`)
- Spring Security JWT Resource Server (with embedded roles & permissions claims)
- JUnit 5 + Spring Boot integration tests

## Repository Layout

```text
build-logic/                 Gradle convention plugins
contracts/asyncapi/          Versioned integration event contracts
docs/                        Documentation (overview, architecture, services, development)
gradle/libs.versions.toml    Central dependency versions
libs/messaging-foundation/   Shared messaging types (event envelope, outbox relay contract)
compose.yaml                 Docker Compose setup (Postgres, Redis, Eureka, RabbitMQ)
services/
├── api-gateway/             Ingress routing & trusted-header forwarding
├── identity-service/        Registration, authentication, JWT issuance, & RBAC
├── tenant-service/          Personal accounts and memberships
├── subscription-service/    Subscription lifecycle management
├── competition-service/     Sports, matches, participants, and events
├── ranking-service/         ELO projection and ranking query API
└── discovery-server/        Netflix Eureka Service Discovery (Port 8761)
```

## Quick Start

1. **Prerequisites:** Java 21, Docker & Docker Compose.
2. **Run Tests:**
   ```bash
   ./gradlew test
   ```
3. **Start Infrastructure & Run Services:**
   ```bash
   docker compose up -d
   ./gradlew :services:api-gateway:bootRun --args='--spring.profiles.active=local'
   ./gradlew :services:identity-service:bootRun --args='--spring.profiles.active=local'
   ```

## Read This First

- **`docs/overview.md`** — canonical overview: architecture, service boundaries, core flows, identity internals, local development, and current status.
- **Reference docs:**
  - `docs/services/*.md` — service-by-service notes
  - `docs/architecture/*.md` — conventions and boundary rules
  - `docs/development/*.md` — local dev, testing, and running guides