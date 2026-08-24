> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# ArenaX Service Boundaries

File này là bản ownership map ngắn. Repo overview đầy đủ nằm ở `../overview.md`; rule cưỡng chế nằm ở `conventions.md`.

## Services

- `identity-service`: credentials, login identifiers (verified email), profile basics, refresh sessions, JWT issuance, RBAC (roles, permissions, account-scoped assignments).
- `tenant-service`: accounts, ownership, memberships.
- `subscription-service`: plan lifecycle.
- `competition-service`: sports, matches, teams, participants.
- `ranking-service`: player rating, leaderboard, ranking history.
- `api-gateway`: ingress routing and cross-cutting HTTP concerns.
- `libs/messaging-foundation`: shared, non-persistent messaging types — `EventEnvelope`, and the outbox relay contract (`PendingOutboxEvent`, `OutboxEventStore`, `OutboxEventRelay`). Each service keeps its own `OutboxEvent` entity and repository; the relay is wired per service via `OutboxEventStoreAdapter` + `OutboxEventRelayConfiguration`.

## Boundary Rules

- Each service owns its own database schema.
- Services communicate via HTTP only when an immediate answer is required.
- Preferred cross-service integration is RabbitMQ events with versioned contracts.
- No cross-service JPA entity, repository, or migration sharing.
- Public identifiers use UUID.

## When You Are Unsure Where Code Should Go

Trả lời 3 câu hỏi trước:

1. service nào own state này?
2. service nào phát event hoặc trả API cho capability này?
3. nếu đổi rule này, service nào phải release cùng?

Nếu câu trả lời làm mờ boundary giữa 2 service, thường là code đang đặt sai chỗ.
