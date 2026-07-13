# Ranking Service

Responsibilities:

- maintain player ELO projection
- store ranking history
- expose ranking query endpoints

Current consumed event:

- `competition.match-completed.v1`

Current API slice:

- `GET /api/v1/rankings/users/{userId}`
