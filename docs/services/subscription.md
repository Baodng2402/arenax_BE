# Subscription Service

Responsibilities:

- manage per-account subscription state
- provision the default plan during onboarding

Current onboarding behavior:

- consumes `tenant.personal-account-created.v1`
- creates one `FREE` subscription per account
- emits `subscription.activated.v1`
