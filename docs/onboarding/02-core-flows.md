# 02. Core Flows

## Flow 1: User Onboarding

Đây là flow quan trọng nhất vì nó nối nhiều service với nhau.

### Mục tiêu business

Khi một user mới đăng ký:

- user được tạo trong Identity
- user có personal account trong Tenant
- account có FREE subscription
- user được gán role mặc định `USER` trong Access
- chỉ sau khi đủ điều kiện thì Identity mới activate user để login thật sự

### Trình tự hiện tại

1. `identity-service` nhận register request.
2. Identity tạo user với status `PROVISIONING`.
3. Identity ghi outbox event `identity.user.registered.v2`.
4. `tenant-service` consume event này ở service layer.
5. Tenant tạo personal account và owner membership.
6. Tenant ghi `tenant.personal-account-created.v1`.
7. `access-service` xử lý personal-account-created.
8. Access tạo permission mặc định, role `USER`, role assignment theo account.
9. Access ghi `access.default-role-granted.v1` và `access.authorization-changed.v1`.
10. `subscription-service` xử lý personal-account-created.
11. Subscription tạo FREE subscription.
12. Subscription ghi `subscription.activated.v1`.
13. `identity-service` nhận tín hiệu authorization và subscription completion.
14. Identity update onboarding progress, set `activeAccountId`, chuyển user sang `ACTIVE`.

### Điều quan trọng cần nhớ

- User vừa register chưa login được ngay nếu onboarding chưa xong.
- Identity không cần gọi đồng bộ sang Access hoặc Subscription để login.
- Identity dùng authorization projection local để đưa `roles` và `permissions` vào JWT.

## Flow 2: Login Và Token Issuance

### Mục tiêu business

Cho phép user `ACTIVE` đăng nhập và nhận JWT có đủ claim cần thiết cho downstream services.

### Trình tự hiện tại

1. User gọi `POST /api/v1/auth/login` qua gateway hoặc trực tiếp vào Identity.
2. `identity-service` verify email và BCrypt password.
3. Nếu user đang `PROVISIONING`, login bị từ chối.
4. Nếu user `ACTIVE`, Identity đọc authorization projection local.
5. Identity issue access token bằng RSA key cục bộ.
6. JWT hiện chứa `account_id`, `roles`, `permissions` cùng các claim tiêu chuẩn.

### Điều quan trọng cần nhớ

- Chỉ Identity được issue JWT.
- Các service khác dự kiến sẽ validate token locally.
- Refresh-token flow chưa hoàn thiện; hiện response login vẫn có placeholder refresh token rỗng.

## Flow 3: Match Result To Ranking

### Mục tiêu business

Khi một trận rank hoàn thành, ranking phải cập nhật ELO cho winner và loser.

### Trình tự hiện tại

1. `competition-service` tạo sport.
2. Competition tạo match.
3. Participant join match.
4. Match được complete bằng score của hai team.
5. Competition ghi `competition.match-completed.v1` vào outbox.
6. `ranking-service` consume event này ở service layer.
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
  - `/api/v1/auth/**` -> Identity
  - `/api/v1/users/**` -> Identity
  - `/api/v1/access/**` -> Access
  - `/api/v1/accounts/**` -> Tenant
  - `/api/v1/subscriptions/**` -> Subscription
  - `/api/v1/sports/**` -> Competition
  - `/api/v1/matches/**` -> Competition
  - `/api/v1/rankings/**` -> Ranking
- Gateway thêm hoặc propagate `X-Request-Id`.

### Điều quan trọng cần nhớ

- Gateway hiện dùng static URI config, chưa dùng Eureka.
- Đây là routing layer cơ bản, chưa phải API gateway đầy đủ với auth/rate limit/circuit breaker hoàn chỉnh.
