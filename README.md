# ArenaX Backend

ArenaX backend is now a Gradle monorepo for multiple Spring Boot 4 services. The repository no longer treats the legacy monolith as the source of truth; active development happens under `services/` with database-per-service and event-driven boundaries.

## Stack

- Java 21
- Spring Boot 4.0.6
- Spring Cloud Gateway 5.0.0
- Gradle Kotlin DSL with convention plugins under `build-logic/`
- PostgreSQL + Flyway per service
- Spring Data JPA
- Spring Security JWT resource server / issuer flow
- RabbitMQ event contracts under `contracts/asyncapi`
- JUnit 5 + Spring Boot integration tests

## Repository Layout

```text
build-logic/                 Gradle convention plugins
contracts/asyncapi/          versioned integration event contracts
docs/architecture/           cross-service boundaries and event rules
docs/development/            local dev and testing guides
docs/services/               service-by-service notes
gradle/libs.versions.toml    central dependency versions
services/
├── api-gateway/
├── identity-service/
├── access-service/
├── tenant-service/
├── subscription-service/
├── competition-service/
└── ranking-service/
```

## Services

- `api-gateway`: ingress routing and request ID propagation.
- `identity-service`: registration, login, onboarding progress, JWT issuance.
- `access-service`: roles, permissions, tenant-scoped role assignments.
- `tenant-service`: personal accounts and memberships.
- `subscription-service`: default `FREE` subscription lifecycle.
- `competition-service`: sports, matches, participants, match completion event.
- `ranking-service`: ELO projection and ranking query API.

## Current Integration Model

- Every service owns its own schema and Flyway migrations.
- Cross-service Java dependencies are not allowed.
- Integration events use the shared AsyncAPI envelope in `contracts/asyncapi/arenax-events.yaml`.
- Current code persists outbox rows in producer services and processes events at the service layer.
- RabbitMQ deployment wiring is intentionally deferred until after source architecture stabilization.

## Build And Test

Run the full repository test suite:

```bash
./gradlew test
```

Run a single service:

```bash
./gradlew :services:identity-service:test
./gradlew :services:competition-service:test
```

Inspect the project graph:

```bash
./gradlew projects
```

## Local Runtime Status

This repository currently focuses on source architecture and service-level tests.

- Docker Compose for the new distributed stack is not implemented yet.
- VPS deployment and CI/CD are not implemented yet.
- Shared runtime defaults live in each service's `src/main/resources/application.yaml`, while local run settings live in `application-local.yaml`.
- Gateway default downstream URLs point to `localhost:8081` through `localhost:8086`.
- Helper scripts are available under `bin/run-service`, `bin/run-local-stack`, and `bin/generate-jwt-keys.sh` to reduce `bootRun` command length.
- Local run guide: `docs/development/running-services.md` (includes JWT key setup, Eureka caveat, and an end-to-end smoke test).

## Documentation Index

- `docs/architecture/service-boundaries.md`
- `docs/architecture/conventions.md`
- `docs/architecture/service-template.md`
- `docs/architecture/event-conventions.md`
- `docs/onboarding/README.md`
- `docs/development/local-development.md`
- `docs/development/intellij-setup.md`
- `docs/development/running-services.md`
- `docs/development/running-the-stack.md`
- `docs/development/git-and-pr-conventions.md`
- `docs/development/testing.md`
- `docs/services/*.md`

## Legacy Removal

The old root monolith source and monolith-specific conventions have been removed from the active architecture. Do not add new code under a root `src/` module; all new implementation belongs in one of the service modules under `services/`.
