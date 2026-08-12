# Tenant Service

Responsibilities:

- create personal and team accounts
- manage memberships (owner membership on creation)
- publish account-created events for onboarding

Current onboarding behavior:

- consumes `identity.user.registered.v2`
- creates one `PERSONAL` account per user
- creates owner membership
- emits `tenant.personal-account-created.v1`

Public API (via gateway, `/api/v1`):

- `GET /accounts` - list the current user's accounts (current context marked `current`)
- `POST /accounts/workspaces` - create a `TEAM` account with an owner membership
- `GET /accounts/{accountId}/memberships` - list memberships for an account the caller belongs to