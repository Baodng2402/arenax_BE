> **Reference spec** - đọc file này khi bạn sắp thêm hoặc review internal synchronous HTTP giữa services.

# ArenaX Internal HTTP API Rules

Internal service-to-service HTTP is for narrow orchestration cases only.

## Route Shape

- Internal routes live under `/internal/v1/**`.
- Public routes live under `/api/v1/**`.
- Health endpoints stay under actuator conventions.

## Caller Rules

- Caller must authorize the user before making an internal call.
- Caller sends only business parameters needed for the downstream action.
- Caller propagates `X-Request-Id`.
- Caller must not forward bearer tokens or `X-Arenax-*` user headers.

## Receiver Rules

- Receiver authorizes the workload identity via mesh/network policy.
- Receiver must not infer end-user identity from caller-provided headers.
- Receiver should validate idempotency keys for retryable write operations.
- Receiver should keep contracts semantic and service-owned; do not share Java DTO jars.

## Resilience Defaults

- Prefer short, bounded call chains.
- Default timeouts: connect 500ms, read 2000ms.
- Default retry policy: no automatic retries for non-idempotent operations.
- Errors should map to stable business-facing categories, not transport stack traces.

## Read Next

- `../../architecture/openfeign-conventions.md`
- `../../how-to/add-an-internal-http-call.md`
