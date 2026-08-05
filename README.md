# ArenaX Backend

ArenaX backend is a Gradle monorepo for Spring Boot microservices. The repository uses database-per-service, event-driven boundaries, and centralized service discovery (Eureka).

## Stack

- Java 17+
- Spring Boot 3.x / Spring Cloud 2023+
- Spring Cloud Gateway
- Gradle Kotlin DSL with convention plugins under `build-logic/`
- PostgreSQL + Flyway per service
- Redis for caching & state management
- Spring Data JPA
- Spring Security JWT Resource Server (with embedded roles & permissions claims)
- JUnit 5 + Spring Boot integration tests

## Repository Layout

```text
build-logic/                 Gradle convention plugins
contracts/asyncapi/          Versioned integration event contracts
docs/architecture/           Cross-service boundaries and event rules
docs/development/            Local dev and testing guides
docs/services/               Service-by-service notes
gradle/libs.versions.toml    Central dependency versions
compose.yaml                 Docker Compose setup (Postgres, Redis, Eureka)
services/
├── api-gateway/             Ingress routing & request forwarding
├── identity-service/        Authentication, JWT issuance, & RBAC (Roles & Permissions)
├── tenant-service/          Personal accounts and memberships
├── subscription-service/    Subscription lifecycle management
├── competition-service/     Sports, matches, participants, and events
├── ranking-service/         ELO projection and ranking query API
└── discovery-server/        Netflix Eureka Service Discovery (Port 8761)
```

## Services & Responsibilities

- **`discovery-server`**: Netflix Eureka registry for service discovery.
- **`api-gateway`**: Ingress routing, request ID propagation, and gateway filters.
- **`identity-service`**: User registration, login, JWT token issuance with embedded `roles` & `permissions` claims, and RBAC management.
- **`tenant-service`**: Personal accounts and tenant memberships.
- **`subscription-service`**: Default `FREE` subscription lifecycle.
- **`competition-service`**: Sports, matches, participants, and completion events.
- **`ranking-service`**: ELO projection and ranking query API.

## Quick Start & Running Locally

1. **Prerequisites:** Java 17+, Docker & Docker Compose.
2. **Run Tests:**
   ```bash
   ./gradlew test
   ```
3. **Start Infrastructure & Run Services:**
   You can start infrastructure (Postgres, Redis, Eureka) using Docker Compose:
   ```bash
   docker compose up -d
   ```
   Then run any service (Spring Boot automatically detects running infrastructure via `spring-boot-docker-compose`):
   ```bash
   ./gradlew :services:api-gateway:bootRun --args='--spring.profiles.active=local'
   ./gradlew :services:identity-service:bootRun --args='--spring.profiles.active=local'
   ```

## Documentation Index

- **Running & Development:**
  - `docs/development/running-the-stack.md` (Local runtime guide)
  - `docs/development/intellij-setup.md` (IDE configuration)
  - `docs/development/testing.md` (Testing guidelines)
- **Architecture & Onboarding:**
  - `docs/onboarding/README.md` (Onboarding guide)
  - `docs/architecture/service-boundaries.md` (Service boundaries)
  - `docs/architecture/conventions.md` (Coding conventions)
