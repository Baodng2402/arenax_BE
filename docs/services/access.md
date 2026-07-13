# Access Service

Responsibilities:

- define permissions and roles
- assign roles per account
- emit authorization change events for downstream projections

Current onboarding behavior:

- consumes `tenant.personal-account-created.v1`
- grants default `USER` role
- emits `access.default-role-granted.v1`
- emits `access.authorization-changed.v1`
