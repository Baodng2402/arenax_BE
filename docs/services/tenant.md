> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# Tenant Service

## Responsibilities

- create personal and team accounts
- manage memberships (owner membership on creation)
- publish account-created events for onboarding

## Owns Data

- `accounts`
- `memberships`
- `outbox_events`

## Consumes

- consumes `identity.user.registered.v2`

## Emits

- creates one `PERSONAL` account per user
- creates owner membership
- emits `tenant.personal-account-created.v1`

## Public API

Via gateway under `/api/v1`:

- `GET /accounts` - list the current user's accounts (current context marked `current`)
- `POST /accounts/workspaces` - create a `TEAM` account with an owner membership
- `GET /accounts/{accountId}/memberships` - list memberships for an account the caller belongs to

## Implementation Notes

- Tenant owns account and membership state; other services should only reference `accountId`.
- Current onboarding flow is event-driven from Identity, not a direct synchronous call.

## Read Next

- `../contracts/openapi/tenant-api.yaml`
- `../contracts/asyncapi/arenax-events.yaml`
- `../architecture/service-boundaries.md`
