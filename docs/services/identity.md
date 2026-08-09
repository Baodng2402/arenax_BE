# Identity Service

Responsibilities:

- register users
- authenticate active users
- track onboarding progress
- persist authorization projections
- issue JWT access tokens with local claims

Key local tables:

- `users`
- `refresh_sessions`
- `onboarding_progress`
- `authorization_projections`
- `outbox_events`

Current emitted event:

- `identity.user.registered.v2`
