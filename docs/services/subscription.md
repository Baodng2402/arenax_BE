> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# Subscription Service

Responsibilities:

- manage per-account subscription state
- provision the default plan during onboarding
- derive entitlements from the current plan

Current onboarding behavior:

- consumes `tenant.personal-account-created.v1`
- creates one `FREE` subscription per account
- emits `subscription.activated.v1`

Emitted events:

- `subscription.activated.v1`
- `subscription.changed.v1`
- `subscription.cancelled.v1`

Public API (via gateway, `/api/v1`):

- `GET /subscriptions/current` - current account's subscription with derived entitlements
- `PATCH /subscriptions/current/plan` - change plan (`FREE`, `PRO`, `TEAM`)
- `POST /subscriptions/current/cancel` - cancel the current account's subscription