> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# ArenaX Service Template

Tài liệu này là template chuẩn để tạo một service mới hoặc một vertical slice mới trong service hiện có.

Mục tiêu là giúp dev mới không phải đoán structure, file nào cần tạo, test nào phải có, và rule nào bắt buộc phải giữ.

## 1. Khi Nào Tạo Service Mới

Chỉ tạo service mới khi thỏa ít nhất các điều kiện sau:

- có business capability đủ độc lập
- có data ownership riêng
- có boundary đủ rõ để không cần share entity/repository với service khác
- có lý do tách runtime hoặc integration, không chỉ vì muốn chia nhỏ code

Không tạo service mới chỉ để:

- chia team theo cảm tính
- tách class lớn mà chưa có bounded context rõ
- tránh refactor code hiện tại

## 2. Cấu Trúc Tối Thiểu Của Một Service Mới

```text
services/<service-name>/
├── build.gradle.kts
└── src/
    ├── main/
    │   ├── java/com/bk/arenax/<service>/
    │   │   ├── <Service>Application.java
    │   │   ├── configuration/
    │   │   ├── controller/
    │   │   ├── domain/entity/
    │   │   ├── domain/enums/
    │   │   ├── dto/request/
    │   │   ├── dto/response/
    │   │   ├── messaging/
    │   │   ├── repository/
    │   │   └── service/
    │   └── resources/
    │       ├── application.yaml
    │       └── db/migration/
    └── test/
        └── java/com/bk/arenax/<service>/
```

## 3. Checklist Tạo Service Mới

### 3.1 Build Wiring

- thêm module vào `settings.gradle.kts`
- tạo `services/<service-name>/build.gradle.kts`
- chọn đúng convention plugin:
  - service có DB: `arenax.persistence-conventions`
  - gateway hoặc service chưa cần DB: `arenax.spring-service-conventions`
- thêm dependency local nếu thật sự cần, ví dụ `jackson-datatype-jsr310`

### 3.2 Bootstrap

- tạo `<Service>Application.java`
- tạo `application.yaml`
- đặt `spring.application.name`

Ví dụ:

```java
@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
```

### 3.3 Flyway

- tạo migration `V1__create_<service>_core.sql`
- tạo bảng theo data ownership thực sự của service
- thêm unique constraints cho invariant chính ngay từ đầu

### 3.4 Test Skeleton

- ít nhất một `@SpringBootTest`
- nếu có HTTP thì dùng `MockMvc`
- nếu có event handler thì test bằng cách gọi handler trực tiếp và assert persistence side effects

## 4. Template Cho Một Vertical Slice Mới

Một slice mới nên đi theo thứ tự này:

1. viết test fail
2. chạy test để xác nhận fail đúng lý do
3. thêm entity/repository/migration tối thiểu
4. thêm service logic tối thiểu
5. thêm controller hoặc handler
6. chạy lại focused test
7. chạy `./gradlew test` ở root
8. update docs nếu public behavior hoặc integration behavior đổi

## 5. Template File Theo Từng Loại Flow

### 5.1 HTTP Create Flow

Ví dụ create resource mới:

```text
controller/<Feature>Controller.java
dto/request/Create<Feature>Request.java
dto/response/<Feature>Response.java
service/<Feature>Service.java
domain/entity/<Feature>.java
repository/<Feature>Repository.java
db/migration/Vn__....sql
test/.../<Feature>ControllerIntegrationTests.java
```

Test tối thiểu phải có:

- create success
- duplicate hoặc invalid input
- persistence assertion

### 5.2 Event Consumer Flow

Ví dụ consume event từ service khác:

```text
messaging/EventEnvelope.java
messaging/<IncomingPayload>.java
service/<IncomingEvent>Handler.java
repository/... nếu cần
domain/entity/... nếu cần state mới
db/migration/Vn__....sql nếu có bảng mới
test/.../<IncomingEvent>HandlerIntegrationTests.java
```

Test tối thiểu phải có:

- first delivery success
- duplicate delivery idempotent
- side effect chính được assert rõ

### 5.3 Event Producer Flow

Nếu slice phát event ra ngoài, phải có thêm:

```text
domain/entity/OutboxEvent.java
repository/OutboxEventRepository.java
messaging/<OutgoingPayload>.java
```

Test tối thiểu phải assert:

- business state được save
- outbox row được ghi cùng flow
- duplicate action không phát thêm event ngoài ý muốn nếu rule yêu cầu idempotent publish

## 6. Template Entity Rules

Một entity mới nên tuân theo pattern sau:

```java
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @PrePersist
    void assignId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
```

Rule:

- giữ entity nhỏ và đúng ownership
- không add relation sang service khác
- nếu cần external reference thì lưu UUID

## 7. Template Repository Rules

Repository chỉ nên chứa:

- method CRUD tiêu chuẩn từ `JpaRepository`
- query methods phản ánh invariant hoặc lookup thực sự đang dùng

Không thêm query method speculative.

Ví dụ tốt:

```java
Optional<Order> findByExternalId(UUID externalId);
boolean existsByMatchId(UUID matchId);
```

Ví dụ không nên:

```java
List<Order> findByStatusAndCreatedAtAfterAndCreatedAtBeforeAndUserIdIn(...)
```

trừ khi flow hiện tại thật sự dùng tới.

## 8. Template Service Rules

Service class nên:

- giữ transaction boundary của use case
- encode invariant rõ ràng
- gọi repository và outbox logic theo đúng local ownership
- không trực tiếp biết chi tiết HTTP transport

Service class không nên:

- parse servlet request
- trả `ResponseEntity`
- gọi service module khác bằng Java dependency

## 9. Template Controller Rules

Controller mới nên:

- annotate route rõ ràng dưới `/api/v1/...`
- nhận request DTO có validation
- trả DTO response
- không chứa business logic ngoài mapping input/output

Ví dụ skeleton:

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(request);
    }
}
```

## 10. Template Event Payload Rules

Payload record local phải mirror contract nhưng nằm trong service của chính nó.

Ví dụ:

```java
public record OrderCreatedPayload(
        UUID orderId,
        UUID userId,
        String status
) {}
```

Không import `OrderCreatedPayload` từ service khác.

## 11. Template Tests

### 11.1 HTTP Integration Test Template

```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    void createOrderPersistsState() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":"11111111-1111-1111-1111-111111111111"}
                                """))
                .andExpect(status().isCreated());

        assertThat(orderRepository.findAll()).hasSize(1);
    }
}
```

### 11.2 Event Handler Test Template

```java
@SpringBootTest
class OrderCreatedHandlerIntegrationTests {

    @Autowired
    private OrderCreatedHandler handler;

    @Autowired
    private OrderProjectionRepository repository;

    @Test
    void handleIsIdempotent() {
        EventEnvelope<OrderCreatedPayload> event = ...;

        handler.handle(event);
        handler.handle(event);

        assertThat(repository.findAll()).hasSize(1);
    }
}
```

## 12. Required Docs Update When Adding A Service

Khi tạo service mới, bắt buộc update:

- `README.md`
- `docs/architecture/service-boundaries.md`
- `docs/services/<service>.md`

Nếu service tạo hoặc consume event mới, update thêm:

- `contracts/asyncapi/arenax-events.yaml`
- `docs/architecture/event-conventions.md` nếu rule mới xuất hiện

## 13. Required Docs Update When Adding A New Slice

Không phải slice nào cũng cần update README, nhưng cần update docs khi:

- thêm endpoint public mới
- thêm event mới
- thêm invariant business quan trọng
- thay đổi onboarding/integration behavior
- thêm command dev/test mới

## 14. Definition Of Ready Trước Khi Code

Trước khi bắt đầu code một service mới hoặc slice mới, dev phải trả lời được:

- service này own dữ liệu gì
- event nào nó consume hoặc produce
- API path của nó là gì
- invariant chính là gì
- test nào sẽ fail đầu tiên

Nếu chưa trả lời được, quay lại design/docs trước.

## 15. Definition Of Done Cho Service Mới

Một service mới chỉ được xem là đủ baseline khi có:

- module Gradle chạy được
- bootstrap app class
- `application.yaml`
- migration `V1`
- ít nhất một integration test xanh
- ít nhất một doc dưới `docs/services/`
- root `./gradlew test` vẫn xanh

## 16. Definition Of Done Cho Slice Mới

Một slice mới chỉ được xem là hoàn thành khi:

- test fail-first đã tồn tại
- focused test đã xanh
- root suite vẫn xanh
- migration và constraints tương ứng đã có
- docs liên quan đã update nếu behavior public/integration đổi
- không tạo vi phạm mới với `docs/architecture/conventions.md`
