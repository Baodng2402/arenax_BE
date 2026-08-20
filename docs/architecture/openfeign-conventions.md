> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# ArenaX OpenFeign Conventions

ArenaX dùng OpenFeign cho các HTTP call nội bộ hẹp giữa service với service.

## Khi Nào Được Dùng

- Chỉ dùng cho orchestration nhỏ, đồng bộ, thật sự cần response ngay.
- Ưu tiên event async nếu flow không cần trả kết quả trong request hiện tại.
- Không tạo chain dài quá 2 hop đồng bộ.

## Bootstrap Mặc Định

- Runtime service có thể khai báo Feign client với `@EnableFeignClients` ở `<Service>Application`.
- Dependency baseline là `org.springframework.cloud:spring-cloud-starter-openfeign`.
- Discovery dùng service name từ Eureka/LoadBalancer, ví dụ `name = "tenant-service"`.

## Default Runtime Config

Tất cả service đang dùng baseline sau trong `application.yaml`:

- `connectTimeout: 500`
- `readTimeout: 2000`
- `loggerLevel: basic`
- `micrometer.enabled: true`

Spring Cloud OpenFeign mặc định dùng `Retryer.NEVER_RETRY`, nên baseline này không tự retry request. Nếu một call idempotent thật sự cần retry, cấu hình riêng trên đúng client đó và ghi rõ trong doc slice.

## Header Rules

- Chỉ propagate `X-Request-Id`.
- Không forward bearer token.
- Không forward `X-Arenax-*` user headers.
- Receiver không được tin caller header như end-user identity.

## Client Shape

Ví dụ tối thiểu:

```java
@FeignClient(name = "tenant-service", path = "/internal/v1/accounts")
public interface TenantAccountClient {

    @PostMapping("/memberships")
    void createMembership(@RequestBody CreateMembershipRequest request);
}
```

## Client Config Pattern

Nếu một client cần config riêng, tạo class local trong chính service đó, không share jar DTO/config xuyên service.

```java
@Configuration
public class TenantAccountClientConfiguration {

    @Bean
    RequestInterceptor requestIdPropagationInterceptor() {
        return template -> {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes instanceof ServletRequestAttributes servletAttributes) {
                String requestId = servletAttributes.getRequest().getHeader("X-Request-Id");
                if (requestId != null && !requestId.isBlank()) {
                    template.header("X-Request-Id", requestId);
                }
            }
        };
    }
}
```

## Error Handling Rules

- Map downstream transport failure thành category business ổn định ở service caller.
- Không leak raw Feign exception message ra public API.
- Timeout, 4xx, 5xx phải được convert ở service layer hiện tại, không để controller tự rơi stack trace.

## Ownership Rules

- Feign request/response model là local DTO của caller.
- Không tạo shared Java contract jar giữa services.
- Internal endpoint phải theo `contracts/internal-api/README.md`.
