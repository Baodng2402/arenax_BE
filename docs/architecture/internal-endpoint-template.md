> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# ArenaX Internal Endpoint Template

Template này dùng khi một service cần expose HTTP nội bộ cho service khác gọi qua OpenFeign.

Rule tổng quát nằm ở `openfeign-conventions.md` và `../contracts/internal-api/README.md`. File này chỉ giữ skeleton tối thiểu để copy nhanh.

## Route Rules

- Prefix: `/internal/v1/**`
- Không expose route này ra gateway public path.
- Path phải semantic theo capability, không generic CRUD nếu business không cần.

## Controller Skeleton

```java
@RestController
@RequestMapping("/internal/v1/accounts")
public class InternalAccountController {

    private final InternalAccountService internalAccountService;

    public InternalAccountController(InternalAccountService internalAccountService) {
        this.internalAccountService = internalAccountService;
    }

    @PostMapping("/memberships")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createMembership(@Valid @RequestBody CreateMembershipRequest request,
                                 @RequestHeader("X-Request-Id") String requestId) {
        internalAccountService.createMembership(request, requestId);
    }
}
```

## Required Rules

- Validate payload with local DTO.
- Accept and propagate `X-Request-Id`.
- Do not read `Authorization` or `X-Arenax-*` as user identity.
- Receiver authorizes workload at infra layer, not via caller-supplied user headers.
- Document caller service in mesh/network policy.

## Read Next

- `openfeign-conventions.md`
- `../how-to/add-an-internal-http-call.md`
- `../contracts/internal-api/README.md`

## Test Minimum

- One integration test for happy path.
- One validation or idempotency/error test.
- Assert service-owned persistence side effect if endpoint mutates state.
