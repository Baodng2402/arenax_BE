> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# Competition Service

## Responsibilities

- manage sports
- manage matches and participants
- complete matches and publish result events

## Owns Data

- `sports`
- `matches`
- `match_participants`
- `outbox_events`

## Consumes

- none currently

## Emits

- `competition.match-completed.v1`

## Public API

- `POST /api/v1/sports`
- `POST /api/v1/matches`
- `POST /api/v1/matches/{matchId}/join`
- `POST /api/v1/matches/{matchId}/complete`

## Implementation Notes

- Competition is the source of truth for sports and match results.
- Ranking must react through events; it must not read Competition tables directly.

## Read Next

- `../contracts/asyncapi/arenax-events.yaml`
- `../architecture/service-boundaries.md`
- `ranking.md`
