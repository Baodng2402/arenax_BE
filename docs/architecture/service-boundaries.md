# ArenaX Service Boundaries

## Services

- `identity-service`: credentials, login identifiers (verified email), profile basics, refresh sessions, JWT issuance, RBAC (roles, permissions, account-scoped assignments).
- `tenant-service`: accounts, ownership, memberships.
- `subscription-service`: plan lifecycle.
- `competition-service`: sports, matches, teams, participants.
- `ranking-service`: player rating, leaderboard, ranking history.
- `api-gateway`: ingress routing and cross-cutting HTTP concerns.

## Boundary Rules

- Each service owns its own database schema.
- Services communicate via HTTP only when an immediate answer is required.
- Preferred cross-service integration is RabbitMQ events with versioned contracts.
- No cross-service JPA entity, repository, or migration sharing.
- Public identifiers use UUID.
