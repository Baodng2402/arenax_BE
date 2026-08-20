> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# Competition Service

Responsibilities:

- manage sports
- manage matches and participants
- complete matches and publish result events

Current API slice:

- `POST /api/v1/sports`
- `POST /api/v1/matches`
- `POST /api/v1/matches/{matchId}/join`
- `POST /api/v1/matches/{matchId}/complete`

Current emitted event:

- `competition.match-completed.v1`
