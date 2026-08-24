> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# 04. Data And Integration Map

File này là onboarding map để trả lời nhanh: service nào own data gì, produce gì, consume gì. Source canonical vẫn là `docs/overview.md`, `docs/contracts/`, và code/test hiện tại.

## Service Ownership Map

### `identity-service`

Own:

- `users` (kèm legacy `email`/`email_verified_at` để tương thích)
- `user_identifiers` (type `EMAIL`, một primary)
- `email_verification_tokens`
- `refresh_sessions`
- `permissions`
- `roles`
- `role_permissions`
- `role_assignments` (unique theo `(user_id, account_id, role_code)`)
- `outbox_events`

Produce:

- `identity.user.registered.v2`
- `identity.user.verification-requested.v1`
- `identity.user.password-reset-requested.v1`

Consume tại service layer:

- (chưa consume event nào — RBAC đọc trực tiếp bảng local của chính Identity)

### `tenant-service`

Own:

- `accounts`
- `memberships`
- `outbox_events`

Produce:

- `tenant.personal-account-created.v1`

Consume tại service layer:

- `identity.user.registered.v2`

### `subscription-service`

Own:

- `subscriptions`
- `outbox_events`

Produce:

- `subscription.activated.v1`
- `subscription.changed.v1`
- `subscription.cancelled.v1`

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
  - `POST /api/v1/auth/register`, `/login`, `/verify-email`, `/refresh`, `/logout`, `/logout-all`, `/request-password-reset`, `/reset-password`
  - `GET/PATCH /api/v1/users/me`
  - `PUT/DELETE /api/v1/users/me/username`
  - `GET/POST /api/v1/users/me/emails`, `PATCH /api/v1/users/me/emails/{emailId}/primary`, `DELETE /api/v1/users/me/emails/{emailId}`
  - `GET /.well-known/jwks.json`
- Tenant:
  - `GET /api/v1/accounts`
  - `POST /api/v1/accounts/workspaces`
  - `GET /api/v1/accounts/{accountId}/memberships`
- Subscription:
  - `GET /api/v1/subscriptions/current`
  - `PATCH /api/v1/subscriptions/current/plan`
  - `POST /api/v1/subscriptions/current/cancel`
- Competition:
  - `POST /api/v1/sports`
  - `POST /api/v1/matches`
  - `POST /api/v1/matches/{matchId}/join`
  - `POST /api/v1/matches/{matchId}/complete`
- Ranking:
  - `GET /api/v1/rankings/users/{userId}`

Gateway routes đang được bật: `/api/v1/auth/**`, `/api/v1/users/**` -> Identity; `/api/v1/accounts/**` -> Tenant; `/api/v1/subscriptions/**` -> Subscription; `/api/v1/sports/**`, `/api/v1/matches/**` -> Competition. Route ranking chưa được bật.

Gateway routes thêm một entrypoint chung, nhưng service ownership vẫn nằm ở module phía sau.

## Event Integration Map

```text
identity.user.registered.v2
  -> tenant-service

tenant.personal-account-created.v1
  -> subscription-service

subscription.activated.v1
  -> (chưa có consumer nào đăng ký)

subscription.changed.v1
  -> (chưa có consumer nào đăng ký)

subscription.cancelled.v1
  -> (chưa có consumer nào đăng ký)

competition.match-completed.v1
  -> ranking-service
```

Các mũi tên trên là consumer `@RabbitListener` gắn queue binding vào topic exchange `arenax.events`; producer publish qua outbox relay (đánh dấu `published_at` sau khi ack).

Lưu ý: các mũi tên trên mô tả integration shape hiện tại trong source. Chi tiết contract nằm ở `../contracts/asyncapi/arenax-events.yaml`.

## Boundary Mistakes Cần Tránh

- Đừng cho Ranking đọc trực tiếp DB của Competition.
- Đừng cho Competition giữ entity `User` từ Identity.
- Đừng gọi đồng bộ sang service khác trong request path chỉ vì tiện implement.
- Đừng share một Java payload class giữa producer và consumer service.
- Đừng thêm code mới vào root `src/`.

## Read Next

- `../architecture/service-boundaries.md` nếu bạn đang phân vân code nên nằm ở service nào.
- `../how-to/add-a-new-event-flow.md` nếu bạn chuẩn bị nối một integration mới.
- `../services/README.md` nếu bạn muốn deep-dive theo từng service.
