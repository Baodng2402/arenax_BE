> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# 05. Current Status And Gaps

## Những Gì Đã Có

- Monolith cũ đã được thay bằng microservice monorepo theo boundary rõ ràng.
- Mỗi service có module Gradle riêng.
- Mỗi service có entity/repository/migration riêng.
- Các flow chính đã có integration tests.
- AsyncAPI contract đã có.
- Gateway routing cơ bản đã có.
- Identity đã issue JWT bằng RSA, kèm claims `roles`/`permissions` từ RBAC local (migration V6: `permissions`, `roles`, `role_permissions`, `role_assignments`).
- Onboarding flow và ranking flow đã có bản source-level chạy trong test.
- Tenant và Subscription đã có public REST slices (`/api/v1/accounts/**`, `/api/v1/subscriptions/**`) qua gateway với trusted headers.
- OpenAPI docs cho identity/tenant/subscription và AsyncAPI contracts đã đồng bộ với code.

## Những Gì Chưa Có

### Messaging Runtime

Đã có:

- outbox relay (`@Scheduled`, topic exchange `arenax.events`, đánh dấu `published_at` sau publish)
- listener adapter (`@RabbitListener` + queue binding) cho tenant, subscription, ranking
- broker topology: RabbitMQ trong `compose.yaml`

Còn thiếu:

- retry / dead-letter handling
- handled-message inbox persistence ở adapter layer (hiện consumer dựa vào idempotency trong handler)

### Auth Runtime Hoàn Chỉnh

Đã hoàn thiện:

- refresh token rotation + reuse detection (reuse → revoke toàn bộ session + 410)
- refresh token lưu dạng SHA-256 hash, session bảng `refresh_sessions` (kèm `account_id`)
- chặn login cho user SUSPENDED/DEACTIVATED (403); login bắt buộc dùng email identifier đã verify (PENDING/user chưa verify → 401)
- multi-email: thêm/verify/chuyển primary/xóa email qua `/api/v1/users/me/emails`; primary email sync vào legacy `users.email`
- username optional unique handle qua `PUT/DELETE /api/v1/users/me/username`
- endpoint `GET/PATCH /api/v1/users/me` — trust header `X-Arenax-*` từ gateway qua `TrustedGatewayAuthenticationFilter`
- cookie `arenax_refresh_token` secure flag config-driven (`arenax.security.cookie.secure`, mặc định false local)
- `account_id` được giữ xuyên qua refresh (migration V5)

Còn thiếu:

- identity chưa validate `accountId` thuộc về user thật qua tenant membership
- gateway security policy cho route ranking (chưa được bật; tenant/subscription/competition đã có)
- service-to-service auth

### Infrastructure

Chưa có:

- Docker Compose cho full stack microservices
- PostgreSQL containers riêng cho local distributed runtime
- CI/CD
- VPS deployment
- centralized logging
- tracing/metrics/alerts
- circuit breaker/resilience policy

### API Maturity

Chưa có hoặc chưa hoàn thiện đầy đủ:

- OpenAPI docs cho competition/ranking slices
- uniform production-grade error model cho toàn repo
- full authorization checks cho mọi business endpoint

## Điều Này Có Nghĩa Gì Với Người Mới Join

Nếu bạn mới vào repo, hãy hiểu rằng:

- source architecture đã rõ
- implementation slice đầu tiên đã có
- nhưng runtime platform vẫn đang ở phase tiếp theo

Nói ngắn gọn: đây là một nền microservice đúng boundary, chưa phải production platform hoàn chỉnh.

## Cách Đọc “Tiến Độ” Cho Đúng

- Nếu bạn muốn hiểu business structure: repo này đã khá rõ.
- Nếu bạn muốn chạy full distributed stack bằng một lệnh: chưa xong.
- Nếu bạn muốn thêm flow mới theo pattern hiện tại: đã có đủ template và conventions.
