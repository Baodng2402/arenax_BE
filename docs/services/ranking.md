> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# Ranking Service

Responsibilities:

- maintain player ELO projection
- store ranking history
- expose ranking query endpoints

Current consumed event:

- `competition.match-completed.v1`

Current API slice:

- `GET /api/v1/rankings/users/{userId}`
