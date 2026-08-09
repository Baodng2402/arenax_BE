# Identity Core Design

## Goal

Move `identity-service` from single-email user records to a large-system identity model where `userId` is the only canonical identity key, login identifiers are modeled separately, and downstream services only depend on stable `userId`-based contracts.

## Decisions

- `users.id` remains the immutable identity key for all cross-service references.
- Emails move out of `users` into `user_identifiers`.
- Phase 1 supports identifier type `EMAIL`; schema leaves room for future `PHONE`, `GOOGLE`, `GITHUB`, and similar types.
- One user can own multiple identifiers.
- One email address can belong to only one user globally.
- Only verified email identifiers are eligible for password reset.
- Username is optional and not part of auth core in this phase.
- `identity.user.registered` becomes `v2` and drops email from the payload.
- Email remains in delivery-boundary events like verification and password reset.

## Service Boundaries

- `identity-service` owns `User`, `UserIdentifier`, credentials, sessions, verification, password reset, and emitted identity events.
- `tenant-service` continues to react to registered users using only `userId` and `displayName`.
- `subscription-service` remains unchanged at the domain level.
- `api-gateway` and JWT stay `sub=userId` centric.

## Phase 1 Scope

- Add `user_identifiers` table and backfill existing primary email data.
- Keep current public auth API shape email-based for register/login/password reset.
- Resolve email lookups through identifiers instead of `users.email`.
- Mark verification at identifier level while preserving current user activation flow.
- Return `primaryEmail` through existing response fields that currently expose `email`.
- Publish `identity.user.registered.v2` and update tenant consumer/contracts.

## Out Of Scope

- Multi-email management endpoints.
- Social login.
- Phone login.
- Workspace-aware account selection changes.
- Runtime broker wiring.

## Migration Strategy

1. Add additive schema for `user_identifiers` and identifier link on verification tokens.
2. Backfill existing user emails into `user_identifiers` as verified primary email when `users.email_verified_at` is present, otherwise unverified primary email.
3. Switch reads and writes to identifiers.
4. Keep legacy `users.email` and `users.email_verified_at` for compatibility in this phase.
5. Update contracts and downstream consumer to `identity.user.registered.v2`.

## Invariants

- Every user must have exactly one primary email identifier in phase 1.
- `normalized_value` is lowercase trimmed email for `EMAIL` identifiers.
- `(type, normalized_value)` is globally unique.
- Verification token consumption verifies the linked identifier and activates the user if still pending.
- Password reset events always use the resolved primary email identifier.
