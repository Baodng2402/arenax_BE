> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# Ranking Service

## Responsibilities

- maintain player ELO projection
- store ranking history
- expose ranking query endpoints

## Owns Data

- `player_rankings`
- `ranking_history`

## Consumes

- `competition.match-completed.v1`

## Emits

- none currently

## Public API

- `GET /api/v1/rankings/users/{userId}`

## Implementation Notes

- Ranking is a projection service; it does not own match lifecycle.
- ELO updates must stay idempotent per `matchId`.

## Read Next

- `../contracts/asyncapi/arenax-events.yaml`
- `competition.md`
- `../architecture/conventions.md`
