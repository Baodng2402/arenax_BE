# Tenant Service

Responsibilities:

- create personal accounts
- manage memberships
- publish account-created events for onboarding

Current onboarding behavior:

- consumes `identity.user.registered.v1`
- creates one `PERSONAL` account per user
- creates owner membership
- emits `tenant.personal-account-created.v1`
