> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# Identity Service

## Responsibilities

- register users and manage their identity
- manage login identifiers (verified email required to log in) and optional username
- authenticate active users with verified identifiers
- issue JWT access tokens with RBAC claims
- define permissions, roles, and role assignments per account
- send email verification and password-reset delivery events

## Owns Data

- `users` (plus legacy `email` / `email_verified_at`)
- `user_identifiers` (type `EMAIL`, one primary per user)
- `email_verification_tokens`
- `refresh_sessions`
- `permissions`, `roles`, `role_permissions`, `role_assignments`
- `outbox_events`

## Consumes

- none currently

## Emits

- `identity.user.registered.v2`
- `identity.user.verification-requested.v1`
- `identity.user.password-reset-requested.v1`

## Public API

Via gateway under `/api/v1`:

- `POST /auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/logout-all`, `/auth/request-password-reset`, `/auth/reset-password`, `/auth/verify-email`
- `GET /users/me`, `PATCH /users/me`
- `PUT /users/me/username`, `DELETE /users/me/username`
- `GET /users/me/emails`, `POST /users/me/emails`
- `PATCH /users/me/emails/{emailId}/primary`, `DELETE /users/me/emails/{emailId}`
- `GET /.well-known/jwks.json`

## Implementation Notes

- Identity is the only service that issues JWTs.
- Authorization projection stays local here; RBAC is not split into a separate service anymore.
- Email is not the identity root. Cross-service references use `userId`.

Current service-layer structure under `com.bk.arenax.identity.service`:

- `RegistrationService`: `register`, `verifyEmail`.
- `AuthenticationService`: `login`, `refresh`, `logout`, `logoutAll`, `refreshTokenTtlSeconds` (issues JWTs, owns `LoginResult`).
- `PasswordResetService`: `requestPasswordReset`, `resetPassword`.
- `ProfileService`: `getProfile`, `updateProfile`.
- `UserEmailService`: `listEmails`, `updateUsername`, `clearUsername`, `addEmail`, `setPrimaryEmail`, `removeEmail`.
- `service/support/`: shared helpers `IdentityTokenHasher`, `IdentityTokenGenerator`, `IdentityEventSerializer`, `EmailNormalizationService`.

## Read Next

- `../contracts/openapi/identity-api.yaml`
- `../contracts/asyncapi/arenax-events.yaml`
- `../architecture/conventions.md`
