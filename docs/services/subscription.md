> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# Subscription Service

## Responsibilities

- manage per-account subscription state
- provision the default plan during onboarding
- derive entitlements from the current plan

## Owns Data

- `subscriptions`
- `outbox_events`

## Consumes

- consumes `tenant.personal-account-created.v1`

## Emits

- creates one `FREE` subscription per account
- emits `subscription.activated.v1`
- `subscription.activated.v1`
- `subscription.changed.v1`
- `subscription.cancelled.v1`

## Public API

Via gateway under `/api/v1`:

- `GET /subscriptions/current` - current account's subscription with derived entitlements
- `PATCH /subscriptions/current/plan` - change plan (`FREE`, `PRO`, `TEAM`)
- `POST /subscriptions/current/cancel` - cancel the current account's subscription

## Implementation Notes

- Subscription state is account-scoped, not user-scoped.
- Entitlements are derived from the stored plan when responses are built.
- `subscription.changed.v1` và `subscription.cancelled.v1` hiện được phát từ public REST actions đổi plan hoặc hủy gói.

## Read Next

- `../contracts/openapi/subscription-api.yaml`
- `../contracts/asyncapi/arenax-events.yaml`
- `../architecture/conventions.md`
