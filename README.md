# ArenaX Backend

`arenax-be` is the backend monorepo for ArenaX, a sports competition platform.

The system supports the core business areas behind the product:

- user identity and authentication
- accounts and memberships
- subscription lifecycle
- sports, matches, and results
- ELO-based ranking

## What This Repository Contains

This repository is organized as a multi-service Spring Boot system. Each service owns its own boundary, persistence model, and business logic. Cross-service communication is event-first, with synchronous HTTP reserved for cases that need an immediate response.

At a high level, the backend is responsible for:

- registering and authenticating users
- managing personal accounts and memberships
- provisioning subscription state after onboarding
- recording sports and match outcomes
- calculating and exposing player rankings

## Core Architecture

- multi-service Spring Boot architecture
- database-per-service boundaries
- RabbitMQ-based event integration using an outbox pattern
- API gateway as the external entry point
- Eureka for service discovery
- JWT-based authentication issued by `identity-service`

Implementation details, build conventions, contracts, and operational notes live under `docs/`.

## Service Overview

The current monorepo includes these main services:

- `api-gateway`: external routing and trust boundary
- `identity-service`: users, authentication, sessions, and RBAC
- `tenant-service`: accounts and memberships
- `subscription-service`: subscription lifecycle
- `competition-service`: sports, matches, and match completion
- `ranking-service`: ranking projection and ELO updates
- `discovery-server`: Eureka registry

The repo also contains shared technical modules under `libs/` and reusable Gradle convention plugins under `build-logic/`.

## Repo Reading Path

If you are new to the codebase, start here:

1. `docs/overview.md` for the canonical architecture and domain map
2. `docs/architecture/README.md` for repo conventions, boundaries, and architecture reading paths
3. `docs/contracts/README.md` for AsyncAPI, OpenAPI, internal API, and security specs
4. `docs/how-to/` for task-oriented implementation guides
5. `docs/development/` for local development and testing workflow
6. `docs/services/README.md` for service-specific notes

## Documentation Boundary

This root `README.md` is intentionally a landing page.

- keep high-level product and repo context here
- keep architecture, implementation, onboarding, and contract details in `docs/`

The canonical entry point is `docs/overview.md`.
