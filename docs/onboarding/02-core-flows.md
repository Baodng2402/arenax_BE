> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# 02. Core Flows

## Flow 1: User Onboarding

Đây là flow quan trọng nhất vì nó nối nhiều service với nhau.

### Mục tiêu business

Khi một user mới đăng ký:

- user được tạo trong Identity với status `PENDING`
- user có primary email identifier
- user verify email rồi mới được login
- user có personal account trong Tenant
- account có FREE subscription trong Subscription

### Trình tự hiện tại

1. `identity-service` nhận register request, tạo user với status `PENDING`.
2. Identity tạo primary `EMAIL` identifier và verification token.
3. Identity ghi outbox event `identity.user.verification-requested.v1` (email chỉ nằm ở event delivery boundary này).
4. User gọi verify-email bằng token.
5. Identity verify identifier, chuyển user thành `ACTIVE`, ghi `identity.user.registered.v2` (chỉ chứa `userId` và `displayName`, không có email).
6. `tenant-service` xử lý event này (hiện tại handler được gọi trực tiếp ở service layer trong test).
7. Tenant tạo personal account và owner membership.
8. Tenant ghi `tenant.personal-account-created.v1`.
9. `subscription-service` xử lý personal-account-created.
10. Subscription tạo FREE subscription.
11. Subscription ghi `subscription.activated.v1`.
12. User đã `ACTIVE` có thể login.

### Điều quan trọng cần nhớ

- User vừa register (`PENDING`, chưa verify email) bị từ chối login với `401`.
- `access-service` không tồn tại; RBAC (roles, permissions, role assignments) nằm trong `identity-service` qua migration `V6__add_rbac_core.sql`. Không có handler nào tự gán role mặc định khi onboarding hiện tại.
- Toàn bộ chuỗi event trên đã được nối runtime: mỗi service producer có outbox relay (`@Scheduled` poll các event chưa `published_at`, publish lên topic exchange `arenax.events` với routing key = event type) và mỗi consumer có `@RabbitListener` trên queue riêng (tenant/subscription/ranking). RabbitMQ nằm trong `compose.yaml`; test disabling qua `arenax.messaging.relay.enabled=false` (producer services) và `spring.rabbitmq.listener.simple.auto-startup=false` (consumer services). Xem [Testing Guide](../development/testing.md#test-messaging-runtime-convention) để biết convention chi tiết.

## Flow 2: Login Và Token Issuance

### Mục tiêu business

Cho phép user `ACTIVE` có email identifier đã verified đăng nhập và nhận JWT có đủ claim cần thiết cho downstream services.

### Trình tự hiện tại

1. User gọi `POST /api/v1/auth/login` qua gateway hoặc trực tiếp vào Identity.
2. `identity-service` resolve email identifier (phải verified), verify BCrypt password.
3. Nếu identifier chưa verified, login bị từ chối `401`.
4. Identity kiểm tra lock, suspended, deactivated state.
5. Identity phát access token + refresh session (có rotation và reuse detection).
6. JWT chứa `account_id`, `roles`, `permissions` cùng các claim tiêu chuẩn (`sub` = userId).

### Điều quan trọng cần nhớ

- Chỉ Identity được issue JWT.
- Email không phải identity root; `sub` luôn là `userId`.
- Refresh-token flow đã hoàn thiện: refresh session có rotate, phát hiện reuse, token được hash khi lưu.
- User có thể login bằng bất kỳ email identifier nào đã verified.

## Flow 3: Match Result To Ranking

### Mục tiêu business

Khi một trận rank hoàn thành, ranking phải cập nhật ELO cho winner và loser.

### Trình tự hiện tại

1. `competition-service` tạo sport.
2. Competition tạo match.
3. Participant join match.
4. Match được complete bằng score của hai team.
5. Competition ghi `competition.match-completed.v1` vào outbox.
6. `ranking-service` xử lý event này ở service layer.
7. Ranking tính ELO mới với initial rating `1000` và `K = 32`.
8. Ranking update projection hiện tại và ghi ranking history.
9. Query `GET /api/v1/rankings/users/{userId}` trả ranking hiện tại.

### Điều quan trọng cần nhớ

- Ranking là projection service, không own match lifecycle.
- Competition là source of truth cho result.
- Handler ở Ranking có idempotency theo `matchId` để tránh apply cùng một result nhiều lần.

## Flow 4: HTTP Entry Qua Gateway

### Mục tiêu business

Cho phép client có một entrypoint HTTP chung.

### Trình tự hiện tại

- Gateway route path theo responsibility:
  - `/api/v1/auth/**` -> Identity (public trừ logout-all)
  - `/api/v1/users/**` -> Identity (protected)
  - `/api/v1/accounts/**` -> Tenant (protected)
  - `/api/v1/subscriptions/**` -> Subscription (protected)
- Gateway thay JWT bằng trusted headers (`X-Arenax-User-Id`, `X-Arenax-Session-Id`, `X-Arenax-Account-Id`, `X-Arenax-Roles`, `X-Arenax-Permissions`) trước khi forward, và strip token cũ.
- Route competition (`/api/v1/sports/**`, `/api/v1/matches/**`) đã bật qua gateway. Route ranking chưa bật.

### Điều quan trọng cần nhớ

- Gateway dùng `lb://` URI service discovery qua Eureka (`discovery-server` có trong `compose.yaml`).
- Đây là routing layer cơ bản, chưa phải API gateway đầy đủ với rate limit/circuit breaker hoàn chỉnh.