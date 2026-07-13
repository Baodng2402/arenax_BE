# 04. Data And Integration Map

## Service Ownership Map

### `identity-service`

Own:

- `users`
- `refresh_sessions`
- `onboarding_progress`
- `authorization_projections`
- `outbox_events`

Produce:

- `identity.user.registered.v1`

Consume tại service layer:

- `access.default-role-granted.v1`
- `access.authorization-changed.v1`
- `subscription.activated.v1`

### `tenant-service`

Own:

- `accounts`
- `memberships`
- `outbox_events`

Produce:

- `tenant.personal-account-created.v1`

Consume tại service layer:

- `identity.user.registered.v1`

### `access-service`

Own:

- `permissions`
- `roles`
- `role_permissions`
- `role_assignments`
- `outbox_events`

Produce:

- `access.default-role-granted.v1`
- `access.authorization-changed.v1`

Consume tại service layer:

- `tenant.personal-account-created.v1`

### `subscription-service`

Own:

- `subscriptions`
- `outbox_events`

Produce:

- `subscription.activated.v1`

Consume tại service layer:

- `tenant.personal-account-created.v1`

### `competition-service`

Own:

- `sports`
- `matches`
- `match_participants`
- `outbox_events`

Produce:

- `competition.match-completed.v1`

### `ranking-service`

Own:

- `player_rankings`
- `ranking_history`

Consume tại service layer:

- `competition.match-completed.v1`

### `api-gateway`

Không own business data.

Own responsibility:

- HTTP ingress routing
- request ID propagation/generation

## HTTP Integration Map

Current public slices:

- Identity:
  - `POST /api/v1/auth/register`
  - `POST /api/v1/auth/login`
- Competition:
  - `POST /api/v1/sports`
  - `POST /api/v1/matches`
  - `POST /api/v1/matches/{matchId}/join`
  - `POST /api/v1/matches/{matchId}/complete`
- Ranking:
  - `GET /api/v1/rankings/users/{userId}`

Gateway routes thêm một entrypoint chung, nhưng service ownership vẫn nằm ở module phía sau.

## Event Integration Map

```text
identity.user.registered.v1
  -> tenant-service

tenant.personal-account-created.v1
  -> access-service
  -> subscription-service

access.default-role-granted.v1
  -> identity-service

access.authorization-changed.v1
  -> identity-service

subscription.activated.v1
  -> identity-service

competition.match-completed.v1
  -> ranking-service
```

## Boundary Mistakes Cần Tránh

- Đừng cho Ranking đọc trực tiếp DB của Competition.
- Đừng cho Competition giữ entity `User` từ Identity.
- Đừng cho Identity gọi đồng bộ sang Access mỗi lần login.
- Đừng share một Java payload class giữa producer và consumer service.
- Đừng thêm code mới vào root `src/`.
