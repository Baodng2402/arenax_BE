# Identity Core Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Introduce identifier-based identity storage while preserving current auth APIs and moving downstream registration contracts to `v2`.

**Architecture:** Keep the current auth flows intact externally, but insert a new `user_identifiers` persistence layer under them. Migrate downstream contracts to use only `userId` and `displayName` so tenant and subscription remain decoupled from login identifiers.

**Tech Stack:** Spring Boot, Spring Security, Spring Data JPA, Flyway, JUnit 5, MockMvc, H2

## Global Constraints

- Preserve existing public auth endpoints and cookie behavior.
- Use additive Flyway migrations only.
- Keep JWT `sub` as `userId` and do not add email claims.
- Use TDD: tests first, verify red before production edits, then green.

---

### Task 1: Lock Behavior With Failing Tests

**Files:**
- Modify: `services/identity-service/src/test/java/com/bk/arenax/identity/RegistrationControllerIntegrationTests.java`
- Modify: `services/identity-service/src/test/java/com/bk/arenax/identity/LoginControllerIntegrationTests.java`
- Modify: `services/identity-service/src/test/java/com/bk/arenax/identity/VerifyEmailControllerIntegrationTests.java`
- Modify: `services/identity-service/src/test/java/com/bk/arenax/identity/UserControllerIntegrationTests.java`
- Modify: `services/tenant-service/src/test/java/com/bk/arenax/tenant/UserRegistrationHandlerIntegrationTests.java`

**Interfaces:**
- Consumes: existing auth and tenant handler APIs.
- Produces: tests that require primary-email-backed responses and `identity.user.registered.v2` payloads.

- [ ] Add red tests for primary email and v2 event payload.
- [ ] Run targeted identity and tenant tests and confirm failure reasons match the intended contract changes.

### Task 2: Add Identifier Persistence And Backfill

**Files:**
- Create: `services/identity-service/src/main/resources/db/migration/V7__add_user_identifiers.sql`
- Create: `services/identity-service/src/main/java/com/bk/arenax/identity/domain/UserIdentifier.java`
- Create: `services/identity-service/src/main/java/com/bk/arenax/identity/domain/UserIdentifierType.java`
- Create: `services/identity-service/src/main/java/com/bk/arenax/identity/repository/UserIdentifierRepository.java`
- Modify: `services/identity-service/src/main/java/com/bk/arenax/identity/domain/EmailVerificationToken.java`

**Interfaces:**
- Produces: `UserIdentifier.primaryEmail(UUID userId, String normalizedEmail, Instant verifiedAt)` and repository lookups by normalized email.

- [ ] Add additive migration for `user_identifiers` and identifier link on verification tokens.
- [ ] Backfill legacy user email values into `user_identifiers`.
- [ ] Add JPA model and repository helpers for primary email lookup.

### Task 3: Switch Identity Logic To Identifiers

**Files:**
- Modify: `services/identity-service/src/main/java/com/bk/arenax/identity/domain/User.java`
- Modify: `services/identity-service/src/main/java/com/bk/arenax/identity/service/UserService.java`
- Modify: `services/identity-service/src/main/java/com/bk/arenax/identity/repository/UserRepository.java`
- Modify: `services/identity-service/src/main/java/com/bk/arenax/identity/infrastructure/security/IdentityUserDetailsService.java`
- Modify: `services/identity-service/src/main/java/com/bk/arenax/identity/infrastructure/security/IdentityUserDetails.java`
- Modify: `services/identity-service/src/main/java/com/bk/arenax/identity/controller/dto/RegisterResponse.java`
- Modify: `services/identity-service/src/main/java/com/bk/arenax/identity/controller/dto/UserProfileResponse.java`
- Modify: `services/identity-service/src/main/java/com/bk/arenax/identity/controller/AuthController.java`

**Interfaces:**
- Consumes: `UserIdentifierRepository` lookups by email.
- Produces: identity flows that read/write primary email through identifiers.

- [ ] Register users by creating both `User` and primary email identifier.
- [ ] Authenticate email/password through identifier lookup.
- [ ] Verify email against the identifier linked to the token.
- [ ] Resolve profile and password reset email from primary identifier.

### Task 4: Move Registered Event To V2

**Files:**
- Modify: `services/identity-service/src/main/java/com/bk/arenax/identity/messaging/UserRegisteredPayload.java`
- Modify: `services/tenant-service/src/main/java/com/bk/arenax/tenant/messaging/UserRegisteredPayload.java`
- Modify: `services/tenant-service/src/main/java/com/bk/arenax/tenant/service/UserRegistrationHandler.java`
- Modify: `contracts/asyncapi/arenax-events.yaml`
- Modify: `contracts/asyncapi/examples/user-registered-v1.json`

**Interfaces:**
- Produces: `identity.user.registered.v2` with payload `{ userId, displayName }`.

- [ ] Update producer payload and event metadata to `v2`.
- [ ] Update tenant consumer fixtures and payload record.
- [ ] Refresh AsyncAPI example to match `v2`.

### Task 5: Refresh Public Contract Docs And Verify

**Files:**
- Modify: `contracts/openapi/identity-api.yaml`

**Interfaces:**
- Consumes: final code behavior.
- Produces: docs aligned with primary email semantics and registered event v2.

- [ ] Update OpenAPI descriptions so `email` fields are explicitly primary email values.
- [ ] Run targeted Gradle tests for identity and tenant.
- [ ] Review diff for unintended surface changes.
