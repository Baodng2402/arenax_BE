# Repo Cleanup: Shared Infra, Package Conventions, Identity Split

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** De-duplicate cross-service infrastructure/event code, normalize package conventions across services, and split the 530-line identity `UserService` into small capability-focused application services.

**Architecture:** Consolidate identical event/outbox code shared across services into a small `libs/messaging-foundation` module; standardize package layout so every service uses the same `controller` / `dto/request` / `dto/response` / `configuration` / `infrastructure/messaging` convention; then decompose `identity-service`'s god service into focused use-case services that keep the existing domain model intact.

**Tech Stack:** Spring Boot, Spring Data JPA, Flyway, RabbitMQ (AMQP), Java 21, Gradle Kotlin DSL, JUnit 5, MockMvc, H2.

## Global Constraints

- Preserve all public API endpoints, request/response shapes, cookie behavior, event names, event versions, and payload schemas (see `contracts/`).
- Do not merge or re-boundary services; do not move code between service module boundaries except into the new shared `libs/messaging-foundation`.
- Do not remove `contracts/` files.
- Do not introduce CQRS, mediator frameworks, generic `BaseUseCase<I,O>`, or premature abstraction. No `Utils` class names — use purpose-named components.
- Business invariants stay in domain entities; only genuinely-reused helpers are extracted.
- Each commit must leave the repo green (compile + relevant tests pass).
- Branch: `cleanup/shared-infra-packages-identity-split` (farmed commits, one commit per logical change).
- Recommended shared-lib location: `libs/messaging-foundation`. Runtime code must NOT live in `build-logic`.

---

### Task 1: Baseline And Branch

**Files:**
- Create: `docs/superpowers/plans/2026-08-20-cleanup-shared-infra-packages-identity-split.md` (this file)

**Interfaces:**
- Consumes: current repo state on `main`.
- Produces: a clean working baseline and the cleanup branch.

- [ ] Verify working tree is clean (`git status --short` empty).
- [ ] Run `./gradlew build` from repo root and confirm all modules compile and all tests pass.
- [ ] Create and switch to branch `cleanup/shared-infra-packages-identity-split`.

### Task 2: Repo Hygiene

**Files:**
- Modify: `.gitignore`
- Delete (tracked): `.DS_Store`
- Inspect: any remaining local artifacts (`bin/test`, `.local/`, `secrets/`, `kls_database.db`)

**Interfaces:**
- Consumes: `.gitignore` already covers `build/`, `secrets/`, `.local/`, `.serena/`, `**/bin/`, `kls_database.db`, `kls_database.lock.db`.
- Produces: a clean tracked file set with no artifact pollution.

- [ ] `git rm --cached .DS_Store` and delete the file.
- [ ] Add any missing ignore entries for locally observed artifacts (e.g. `*.db` if broader).
- [ ] Run `git status --short` and confirm only intended files remain.
- [ ] Commit: `chore: remove tracked artifacts and tighten ignores`.

### Task 3: Shared Event Envelope And Outbox Foundation

**Files:**
- Create: `libs/messaging-foundation/build.gradle.kts`
- Create: `libs/messaging-foundation/src/main/java/com/bk/arenax/messaging/EventEnvelope.java`
- Create: `libs/messaging-foundation/src/main/java/com/bk/arenax/messaging/OutboxEvent.java` (only if JPA schema is identical in 100% of consumers; otherwise keep per-service entity)
- Create: `libs/messaging-foundation/src/main/java/com/bk/arenax/messaging/OutboxEventRepository.java` (only under the same condition as above)
- Modify: `settings.gradle.kts` (include `libs:messaging-foundation`)
- Modify: service `build.gradle.kts` files to depend on the new module
- Delete: per-service duplicate `EventEnvelope.java` (5 copies)

**Interfaces:**
- Consumes: identical `EventEnvelope` implementations already present in each service (verified diff-only-on-package).
- Produces: `com.bk.arenax.messaging.EventEnvelope<T>` shared by all services; per-service payload records stay in each service.

- [ ] Create `libs/messaging-foundation` Gradle module (plain Java + Jackson dependency) and register it in `settings.gradle.kts`.
- [ ] Move the canonical `EventEnvelope` into the shared module; update all 5 services to import it and delete their local copies.
- [ ] Compare `OutboxEvent` JPA mapping across services; if identical, move it and the repository to the shared module; if not, stop at helper-level sharing and document the difference.
- [ ] Run `./gradlew build`; fix imports/compile errors.
- [ ] Commit: `refactor: introduce shared event envelope and outbox foundation`.

### Task 4: Package Conventions

**Files:**
- Modify (move): `services/ranking-service`, `services/competition-service`, `services/tenant-service`, `services/subscription-service`, `services/identity-service` — package and directory moves only, no behavior change.

**Interfaces:**
- Consumes: current package layout described in repo survey (some services use `controller/dto`, others `dto/request`+`dto/response`; `config` vs `configuration`; `messaging` vs `infrastructure/messaging`).
- Produces: one agreed convention — controllers in `controller`, DTOs in `dto/request` + `dto/response`, config in `configuration`, `infrastructure/messaging` for relay/consumers/broker config, pure payload/contract types in one agreed `messaging` package.

- [ ] Apply moves from smallest to largest service (ranking → competition → tenant → subscription → identity) updating all imports and test classes.
- [ ] Run `./gradlew build` after each service migration.
- [ ] Commit per service migration group, ending with: `refactor: align messaging and dto package conventions`.

### Task 5: Characterize Identity UserService

**Files:**
- Create: responsibility map + strengthened characterization tests under `services/identity-service/src/test/java/com/bk/arenax/identity/`

**Interfaces:**
- Consumes: `UserService` methods `register`, `verifyEmail`, `login`, `refresh`, `logout`, `logoutAll`, `requestPasswordReset`, `resetPassword`, `refreshTokenTtlSeconds`, `getProfile`, `updateProfile`, `listEmails`, `updateUsername`, `clearUsername`, `addEmail`, `setPrimaryEmail`, `removeEmail`; domain entities and repositories.
- Produces: tests pinning login-lock, refresh-reuse revocation, verification-token lifecycle, password-reset token lifecycle, primary-email switching, and username uniqueness.

- [ ] Document the full responsibility map of `UserService` in the plan/spec doc.
- [ ] Write failing characterization tests for the flows above (login lock, refresh reuse revocation, verification token lifecycle, password reset token lifecycle, primary email switching, username uniqueness).
- [ ] Run tests and confirm they fail for the intended reason (behavior not yet pinned).
- [ ] Implement minimal behavior to pass, keeping behavior identical.
- [ ] Commit: `test: expand coverage for identity service split`.

### Task 6: Extract Shared Identity Helpers

**Files:**
- Create (only if reused by 2+ future services): `IdentityTokenHasher`, `IdentityTokenGenerator`, `IdentityEventSerializer`, `EmailNormalizationService` under `services/identity-service/src/main/java/com/bk/arenax/identity/infrastructure/` or `service/` as appropriate.

**Interfaces:**
- Consumes: repeated private helpers inside `UserService` (`hashToken`, `generateOpaqueToken`, `writePayload`, `normalizeEmail`).
- Produces: purpose-named components (no `Utils`) available to the new capability services.

- [ ] Extract each helper only if at least two of the new services reuse it; otherwise leave it private.
- [ ] Update `UserService` to use the new components.
- [ ] Run identity tests; confirm green.
- [ ] Commit: `refactor: extract shared identity service helpers`.

### Task 7: Extract Registration And Verification Flows

**Files:**
- Create: `services/identity-service/src/main/java/com/bk/arenax/identity/service/RegistrationService.java`
- Modify: controllers and tests that call `register` / `verifyEmail`.

**Interfaces:**
- Consumes: `UserService.register`, `UserService.verifyEmail` behavior (including outbox events `identity.user.verification-requested.v1` and `identity.user.registered.v2`).
- Produces: `RegistrationService.register(...)`, `RegistrationService.verifyEmail(...)` with identical behavior; controllers re-pointed at the new service.

- [ ] Move `register` and `verifyEmail` into `RegistrationService` preserving exact behavior including outbox event emission.
- [ ] Update callers and tests.
- [ ] Run identity tests; confirm green.
- [ ] Commit: `refactor: extract identity registration and verification flows`.

### Task 8: Extract Authentication Flows

**Files:**
- Create: `services/identity-service/src/main/java/com/bk/arenax/identity/service/AuthenticationService.java`
- Modify: controllers and tests that call `login`, `refresh`, `logout`, `logoutAll`, `refreshTokenTtlSeconds`.

**Interfaces:**
- Consumes: `UserService` auth methods and `LoginResult` record.
- Produces: `AuthenticationService.login(...)`, `refresh(...)`, `logout(...)`, `logoutAll(...)`, `refreshTokenTtlSeconds()` with identical behavior.

- [ ] Move auth methods into `AuthenticationService` preserving exact behavior.
- [ ] Update callers and tests.
- [ ] Run identity tests; confirm green.
- [ ] Commit: `refactor: extract identity authentication flows`.

### Task 9: Extract Password Reset Flows

**Files:**
- Create: `services/identity-service/src/main/java/com/bk/arenax/identity/service/PasswordResetService.java`
- Modify: controllers and tests that call `requestPasswordReset`, `resetPassword`.

**Interfaces:**
- Consumes: `UserService` password reset methods (including outbox event `identity.user.password-reset-requested.v1`).
- Produces: `PasswordResetService.requestPasswordReset(...)`, `resetPassword(...)` with identical behavior.

- [ ] Move password reset methods into `PasswordResetService` preserving exact behavior.
- [ ] Update callers and tests.
- [ ] Run identity tests; confirm green.
- [ ] Commit: `refactor: extract identity password reset flows`.

### Task 10: Extract Profile And Email Management Flows

**Files:**
- Create: `services/identity-service/src/main/java/com/bk/arenax/identity/service/ProfileService.java`
- Create: `services/identity-service/src/main/java/com/bk/arenax/identity/service/UserEmailService.java`
- Modify: controllers and tests that call `getProfile`, `updateProfile`, `listEmails`, `updateUsername`, `clearUsername`, `addEmail`, `setPrimaryEmail`, `removeEmail`.

**Interfaces:**
- Consumes: `UserService` profile/email methods.
- Produces: `ProfileService.getProfile/updateProfile`, `UserEmailService.listEmails/updateUsername/clearUsername/addEmail/setPrimaryEmail/removeEmail` with identical behavior.

- [ ] Move profile methods into `ProfileService`.
- [ ] Move email management methods into `UserEmailService`.
- [ ] Update callers and tests.
- [ ] Run identity tests; confirm green.
- [ ] Commit: `refactor: extract identity profile and email management flows`.

### Task 11: Remove God-Service Role From Identity UserService

**Files:**
- Delete: `services/identity-service/src/main/java/com/bk/arenax/identity/service/UserService.java` (or reduce to a thin facade for one commit, then delete).

**Interfaces:**
- Consumes: all flows already moved to capability services in Tasks 6-10.
- Produces: no remaining `UserService` god service; controllers depend only on focused services.

- [ ] Remove `UserService` entirely (or keep thin facade for one commit then delete).
- [ ] Run `./gradlew build` and all identity tests; confirm green.
- [ ] Commit: `refactor: remove god-service role from identity user service`.

### Task 12: Final Verification And Docs

**Files:**
- Modify: `docs/superpowers/specs/2026-08-20-cleanup-shared-infra-packages-identity-split-design.md` or the plan doc itself with a conventions section.
- Modify: `README.md` service layout section if it lists module layout.

**Interfaces:**
- Consumes: completed refactors.
- Produces: documented conventions (package layout, shared messaging module, service capability boundaries) and a verified build.

- [ ] Update docs: shared `libs/messaging-foundation` layout, package convention, identity service capability boundaries.
- [ ] Run full `./gradlew build` from repo root; confirm everything compiles and all tests pass.
- [ ] Run `git log --oneline` and review the farmed commit sequence.
- [ ] Commit: `docs: document cleanup conventions and service boundaries`.