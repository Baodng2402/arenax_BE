> **Reference spec** - đọc file này khi bạn thay đổi access-token shape, JWKS behavior, hoặc gateway JWT validation.

# ArenaX JWT Profile

## Scope

This document defines the only access-token profile accepted from public clients at the ArenaX API Gateway.

## Signing Model

- Only `identity-service` signs end-user access tokens.
- Algorithm is `RS256` only.
- Identity publishes the public key set at `/.well-known/jwks.json`.
- Private key material never leaves Identity runtime or secret manager.
- Every key must have a stable `kid`.

## Required Claims

- `iss`: `arenax-identity`
- `aud`: `arenax-api`
- `sub`: ArenaX user UUID
- `jti`: unique token UUID
- `sid`: refresh-session UUID that produced the access token
- `token_version`: user token version integer for bulk revocation
- `account_id`: optional ArenaX account UUID for current business context
- `roles`: string array for the selected account context
- `permissions`: string array for the selected account context
- `iat`, `nbf`, `exp`: standard temporal claims

## Lifetime

- Access tokens are short-lived and target 10 minutes.
- Refresh uses an opaque random token in an HttpOnly cookie, not a JWT.
- Gateway rejects expired tokens and never tries to refresh automatically.

## Validation Rules At Gateway

- Require `alg=RS256`.
- Resolve key by `kid` from JWKS.
- Validate signature, issuer, audience, `nbf`, `iat`, `exp`.
- Reject tokens missing `sub`, `sid`, or `token_version`.
- Reject malformed `roles` or `permissions` claims.

## Downstream Propagation

After validation, Gateway removes inbound spoofable identity headers and injects:

- `X-Arenax-User-Id`
- `X-Arenax-Session-Id`
- `X-Arenax-Account-Id` when present
- `X-Arenax-Roles`
- `X-Arenax-Permissions`
- `X-Request-Id`

Business services must trust these headers only on `/api/**` traffic that is guaranteed by infrastructure to originate from Gateway.

## Read Next

- `gateway-trust-boundary.md`
- `../../services/identity.md`
- `../../services/api-gateway.md`
