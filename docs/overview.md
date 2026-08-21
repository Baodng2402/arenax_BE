# ArenaX Overview

> **Doc canonical — đọc đầu tiên sau khi mở repo.** Tất cả docs khác là reference/archive; nội dung mâu thuẫn lấy doc này làm chuẩn.

## 1. Repo này là gì

`arenax-be` là backend monorepo của ArenaX — nền tảng tổ chức thi đấu thể thao (tạo giải, ghép trận, xếp hạng ELO, quản lý tài khoản + gói đăng ký).

Kiến trúc: **multi-service Spring Boot, database-per-service, giao tiếp qua event (RabbitMQ) là chính, HTTP chỉ khi cần trả lời ngay**. Mỗi service sở hữu schema riêng, tham chiếu chéo chỉ bằng UUID — không share entity/JPA/migration giữa các service.

## 2. Tech stack (hiện tại)

| Thành phần | Giá trị |
|---|---|
| Java | Toolchain 21 |
| Spring Boot | 4.0.6 |
| Spring Cloud | Gateway 5.0.0, Netflix (Eureka) 5.0.0 |
| Build | Gradle Kotlin DSL + convention plugins trong `build-logic/` |
| DB | PostgreSQL + Flyway (per service), H2 cho test |
| Messaging | RabbitMQ, outbox pattern → topic exchange `arenax.events` |
| Discovery | Eureka (discovery-server) |
| Auth | Spring Security + JWT resource server (RSA, do identity-service cấp) |
| Test | JUnit 5, @SpringBootTest + MockMvc, H2 |

Lưu ý: `compose.yaml` có khai báo Redis (6379) nhưng chưa được dùng ở runtime.

## 3. Monorepo layout

```
arenax-be/
├── build-logic/                 # Convention plugins Gradle (java, spring-service, persistence)
├── contracts/
│   └── asyncapi/                # Hợp đồng event (arenax-events.yaml + examples)
├── docs/
│   ├── overview.md              # ⭐ DOC NÀY — canonical
│   ├── architecture/            # Reference: conventions, boundaries, event-conventions...
│   ├── development/             # Reference: local dev, git/PR conventions, testing...
│   ├── onboarding/              # Reference: system tour, core flows, glossary...
│   ├── operations/              # Reference: security mesh
│   ├── services/                # Reference: chi tiết từng service
├── gradle/libs.versions.toml    # Version catalog
├── libs/
│   └── messaging-foundation/    # EventEnvelope + outbox relay contract (dùng chung)
├── services/                    # 7 services (xem mục 4)
├── compose.yaml                 # Postgres 5432, Redis 6379, discovery-server 8761, RabbitMQ
└── settings.gradle.kts          # include: libs:messaging-foundation + 7 services
```

## 4. Service map

| Service | Port (local) | Owns (data) | Produces | Consumes |
|---|---|---|---|---|
| `api-gateway` | 8080 | — (routing) | — | — |
| `identity-service` | 8081 | users, user_identifiers, tokens, refresh_sessions, RBAC (roles/permissions/role_assignments), outbox | `identity.user.registered.v2`, `identity.user.verification-requested.v1`, `identity.user.password-reset-requested.v1` | — |
| `tenant-service` | 8083 | accounts, memberships | `tenant.personal-account-created.v1` | `identity.user.registered.v2` |
| `subscription-service` | 8084 | subscriptions | `subscription.activated.v1`, `subscription.changed.v1`, `subscription.cancelled.v1` | `tenant.personal-account-created.v1` |
| `competition-service` | 8085 | sports, matches, match_participants | `competition.match-completed.v1` | — |
| `ranking-service` | 8086 | player_rankings, ranking_history | — | `competition.match-completed.v1` |
| `discovery-server` | 8761 | — (Eureka registry) | — | — |

> Ghi chú lịch sử: `access-service` cũ đã **merge vào identity-service** (RBAC qua migration `V6__add_rbac_core.sql`). Không còn access-service.

## 5. Boundaries & integration model

- **Database-per-service**: mỗi service có schema + Flyway migration riêng; nghiêm cấm share JPA entity/repository/migration/DTO giữa các service. Điểm share hợp lệ duy nhất: `libs/messaging-foundation` (EventEnvelope + outbox relay contract — không chứa entity), contract files, test utils.
- **`libs/` vs `contracts/`**: `libs/` = shared implementation (code import vào service, hiện chỉ có `libs/messaging-foundation`); `contracts/` = shared agreement (spec mô tả giao tiếp: AsyncAPI, OpenAPI, security). Thứ chỉ mô tả giao tiếp thì để `contracts/`; thứ services cần import để chạy mới vào `libs/`. Không share entity/repository/business service/DTO/migration qua `libs/`.
- **HTTP chỉ khi cần câu trả lời ngay** (ví dụ: login trả JWT). Mọi thứ khác đi qua **event, versioned, có hợp đồng AsyncAPI** (`contracts/asyncapi/`).
- **Outbox pattern**: service ghi `outbox_events` trong cùng transaction nghiệp vụ → relay (`@Scheduled`, poll 5s) publish lên exchange `arenax.events`, routing key = `eventType`. Consumer dùng `@RabbitListener` trên queue riêng của mình.
- **Event envelope** (JSON): `eventId` (UUID, unique toàn cục, để idempotency), `eventType`, `eventVersion`, `occurredAt`, `correlationId` (business key: userId/accountId/matchId), `producer`, `payload`.
- **Gateway trust boundary**: gateway là nơi duy nhất verify JWT của end-user. Với route đã authenticate, gateway đổi JWT thành trusted headers: `X-Arenax-User-Id`, `X-Arenax-Session-Id`, `X-Arenax-Account-Id`, `X-Arenax-Roles`, `X-Arenax-Permissions` (đồng thời strip header `Authorization` cũ).
- **Route gateway hiện bật**: `/api/v1/auth/**` + `/users/**` → identity, `/accounts/**` → tenant, `/subscriptions/**` → subscription, `/api/v1/sports/**` + `/api/v1/matches/**` → competition. Route ranking **chưa bật**.
- **Chỉ identity cấp JWT**. Email không phải identity root — identity dùng `user_identifiers` (kiểu EMAIL, một primary, verified bắt buộc để login/reset).

## 6. Core flows

### Flow 1 — User onboarding (event chain)
```
register → identity tạo User PENDING + user_identifier EMAIL (primary) + token xác thực
        → outbox identity.user.verification-requested.v1
verify-email → User ACTIVE → outbox identity.user.registered.v2 (userId + displayName)
        → tenant nhận event → tạo Account PERSONAL + Membership OWNER
        → outbox tenant.personal-account-created.v1
        → subscription nhận event → tạo gói FREE (ACTIVE)
        → outbox subscription.activated.v1
        → user có thể login
```

### Flow 2 — Login + token
- `POST /api/v1/auth/login` với email đã verify (chưa verify → 401), check lock/suspended/deactivated.
- Trả access token (JWT, claims: `sub`=userId, `account_id`, `roles`, `permissions`) + refresh token (rotation, phát hiện reuse → thu hồi toàn bộ session).
- `POST /refresh`, `POST /logout`, `POST /logout-all`.

### Flow 3 — Match → Ranking
```
competition: tạo sport/match → join → complete (ghi điểm)
        → outbox competition.match-completed.v1 (winners/losers)
        → ranking: cập nhật ELO (khởi điểm 1000, K=32) + ranking_history
        → GET /api/v1/rankings/users/{userId} (idempotent theo matchId)
```

## 7. Identity service — cấu trúc nội bộ (sau cleanup)

`com.bk.arenax.identity.service` — tách theo capability, không còn god-service:

| Service | Trách nhiệm |
|---|---|
| `RegistrationService` | register, verifyEmail |
| `AuthenticationService` | login, refresh, logout, logoutAll, refreshTokenTtlSeconds (cấp JWT, sở hữu `LoginResult`) |
| `PasswordResetService` | requestPasswordReset, resetPassword |
| `ProfileService` | getProfile, updateProfile |
| `UserEmailService` | listEmails, updateUsername, clearUsername, addEmail, setPrimaryEmail, removeEmail |

Helpers dùng chung trong `service/support/`: `IdentityTokenHasher`, `IdentityTokenGenerator`, `IdentityEventSerializer`, `EmailNormalizationService`.

## 8. Local development path

1. `docker compose up -d` — Postgres (5432), Redis (6379), discovery-server (8761), RabbitMQ. (Compose được Spring Boot auto-detect qua `spring-boot-docker-compose`.)
2. `./gradlew test` — chạy toàn bộ test (integration-first, H2, relay/listener bị tắt trong test).
3. Chạy service:
   ```bash
   ./gradlew :services:identity-service:bootRun --args='--spring.profiles.active=local'
   ```
   Persistence services **bắt buộc** profile `local` (nếu không: `Failed to configure a DataSource: 'url' attribute is not specified`).
4. Gateway health check: `localhost:8080/actuator/health`; Eureka dashboard: `localhost:8761`.

## 9. Docs map

| Vai trò | Đường dẫn |
|---|---|
| **Canonical (đọc trước)** | `README.md` → `docs/overview.md` |
| Reference — conventions | `docs/architecture/conventions.md`, `service-boundaries.md`, `event-conventions.md` |
| Reference — development | `docs/development/local-development.md`, `running-the-stack.md`, `git-and-pr-conventions.md`, `testing.md` |
| Reference — services | `docs/services/identity.md` (có internal structure), `tenant.md`, `subscription.md`, `competition.md`, `ranking.md`, `api-gateway.md` |
| Reference — onboarding | `docs/onboarding/01-system-tour.md` → `02-core-flows.md` → `03-domain-glossary.md` → `04-data-and-integration-map.md` |
| Reference — operations | `docs/operations/service-mesh-security.md` |

## 10. Current status & gaps

**Đã có:** monorepo theo boundary; hợp đồng AsyncAPI; RSA JWT kèm claims roles/permissions; onboarding + ranking flow chạy ở mức source/test; REST tenant/subscription qua gateway trusted headers; OpenAPI cho identity/tenant/subscription; outbox relay dùng chung qua `libs/messaging-foundation`.

**Chưa có (ưu tiên tiếp theo):** CI/CD + deploy; messaging runtime (retry/DLQ/inbox); consumer gửi email thật (verification/password-reset); route gateway + OpenAPI cho competition/ranking; validate `account_id` qua tenant membership; service-to-service auth; observability (logging/tracing/metrics); Redis chưa dùng.

## 11. Glossary (ngắn)

- **User**: PENDING / ACTIVE / SUSPENDED / DEACTIVATED.
- **Account**: PERSONAL hoặc TEAM; **Membership**: OWNER hoặc MEMBER.
- **UserIdentifier**: kiểu EMAIL, có thể nhiều email, một primary; phải verified mới login/reset. Username: optional, unique, không dùng để login.
- **Subscription**: plan FREE / PRO / TEAM, status ACTIVE / CANCELLED.
- **OutboxEvent**: event chờ publish (relay → `arenax.events`); `correlationId` = business key (userId/accountId/matchId).
- **RBAC**: Role/Permission/RoleAssignment nằm trong identity, unique `(user_id, account_id, role_code)`; JWT claims lấy từ DB local qua `RbacService`.