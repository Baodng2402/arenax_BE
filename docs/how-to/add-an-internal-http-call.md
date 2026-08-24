# How To Add An Internal HTTP Call

File này dùng khi event không phù hợp và bạn thực sự cần một synchronous service-to-service HTTP call.

## Chỉ Dùng Khi Nào?

Chỉ dùng internal HTTP nếu:

- caller cần response ngay trong cùng request
- flow là orchestration hẹp, không phải propagation bất đồng bộ
- synchronous chain vẫn giữ ngắn (tối đa khoảng 2 hop)

Nếu flow không cần response ngay, quay lại event-first.

## Default Rules

- endpoint nằm dưới `/internal/v1/**`
- không expose route này qua gateway public path
- chỉ propagate `X-Request-Id`
- không forward bearer token
- không forward `X-Arenax-*` user headers
- receiver không được coi caller header là end-user identity
- request/response DTO là local model của caller, không share contract jar

## Implementation Steps

### 1. Define the receiver endpoint

Ở service nhận:

- tạo controller dưới `/internal/v1/**`
- validate input bằng local DTO
- delegate vào service local
- nếu mutate state, giữ invariant và idempotency ở local service/repository

### 2. Create the caller client

Ở service gọi:

- bật `@EnableFeignClients` nếu service chưa có
- tạo `@FeignClient(name = "<service-name>", path = "/internal/v1/..." )`
- để DTO request/response local trong caller service

### 3. Propagate only request tracing

Nếu cần interceptor, chỉ forward `X-Request-Id`.

Không forward:

- `Authorization`
- `X-Arenax-User-Id`
- `X-Arenax-Session-Id`
- `X-Arenax-Account-Id`
- `X-Arenax-Roles`
- `X-Arenax-Permissions`

### 4. Map downstream failures

Ở service caller:

- convert timeout / 4xx / 5xx thành lỗi business ổn định
- không leak raw Feign exception message ra public API

## Test Minimum

Ít nhất nên có:

- một test receiver happy path
- một test validation hoặc invariant violation
- một test caller-side mapping cho downstream error quan trọng nhất

## Review Checklist

- flow này có thật sự cần synchronous response không?
- route đã ở `/internal/v1/**` chưa?
- header propagation đã đúng chưa?
- caller có đang lén dùng downstream như shared service layer không?
- DTO có bị shared xuyên service không?
- docs/contracts/internal-api/README.md có cần update không?

## Related Docs

- `../architecture/openfeign-conventions.md`
- `../architecture/internal-endpoint-template.md`
- `../contracts/internal-api/README.md`
