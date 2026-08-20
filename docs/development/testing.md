> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# Testing Guide

## Test Style

- Integration-first TDD for new vertical slices
- `@SpringBootTest` for service wiring and persistence checks
- `MockMvc` for HTTP entrypoints
- H2 for current service-level tests through shared Gradle conventions

## Required Checks

```bash
./gradlew test
```

Run a focused suite while iterating:

```bash
./gradlew :services:identity-service:test --tests "com.bk.arenax.identity.*"
./gradlew :services:competition-service:test --tests "com.bk.arenax.competition.*"
```

## Current Coverage

- Identity: registration, duplicate email, provisioning login rejection, onboarding completion, JWT claim issuance
- Tenant: user registration handling, account creation, membership creation, outbox publication
- Access: default role grant, authorization projection events, idempotency
- Subscription: default FREE subscription creation and activation outbox event
- Competition: sport creation, match creation, join flow, completion, match-completed outbox event
- Ranking: ELO update, idempotency, query endpoint
- Gateway: route property binding and request ID propagation
