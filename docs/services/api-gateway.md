> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# API Gateway

## Responsibilities

- route external HTTP traffic to service modules
- propagate or generate `X-Request-Id`
- verify end-user JWT on protected routes
- replace JWT with trusted headers for downstream business services
- expose actuator health

## Owns Data

- none

## Consumes

- HTTP requests from external clients

## Emits

- forwarded HTTP requests to downstream services with trusted context headers

## Public API

- acts as the single public HTTP entrypoint for the repo
- current routed areas include auth/users, accounts, subscriptions, sports, and matches
- ranking route is not enabled yet in the shared default configuration

## Implementation Notes

- Gateway is the trust boundary for end-user authentication.
- Downstream service URLs resolve by discovery in the intended shape, but local defaults point to the ports below.

Current default downstream URLs:

- identity: `http://localhost:8081`
- tenant: `http://localhost:8083`
- subscription: `http://localhost:8084`
- competition: `http://localhost:8085`
- ranking: `http://localhost:8086`

## Read Next

- `../contracts/security/gateway-trust-boundary.md`
- `../architecture/openfeign-conventions.md`
- `../operations/service-mesh-security.md`
