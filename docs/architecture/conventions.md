## ArenaX Engineering Conventions

### 1. Scope

Tài liệu này là bộ quy tắc chính thức cho repository ArenaX backend sau khi tách sang microservices.

Mọi code mới phải ưu tiên tuân theo tài liệu này hơn thói quen từ monolith cũ.

### 2. Repository Rules

- Không tạo code mới dưới root `src/`.
- Mọi implementation mới phải nằm dưới `services/<service-name>/`.
- Không tạo module `common-domain`, `shared-entity`, `shared-dto`, hoặc các module tương tự để tái sử dụng business model giữa services.
- Chỉ được chia sẻ các concern kỹ thuật ổn định như Gradle conventions, test support, và contract files.
- Không thêm Docker Compose, CI/CD, hay deployment scripts vào design của service nếu chưa thật sự cần cho source architecture.

### 3. Monorepo Layout

```text
build-logic/                  Gradle conventions dùng chung
contracts/asyncapi/           event contracts versioned
docs/architecture/            boundary, conventions, integration rules
docs/development/             local dev và testing rules
docs/services/                service-specific notes
services/
  <service-name>/
    src/main/java/...
    src/main/resources/
    src/test/java/...
```

### 4. Service Ownership

Mỗi service phải sở hữu trọn vẹn:

- database schema riêng
- Flyway migrations riêng
- JPA entities riêng
- repositories riêng
- REST controllers riêng
- event payload classes cục bộ của chính service đó
- tests riêng

Mỗi service không được sở hữu hoặc ghi trực tiếp vào bảng của service khác.

### 5. Service Boundaries

- `identity-service`: credentials, onboarding progress, authorization projection, token issuance.
- `access-service`: roles, permissions, tenant-scoped role assignments.
- `tenant-service`: accounts và memberships.
- `subscription-service`: subscription state per account.
- `competition-service`: sports, matches, participants, results.
- `ranking-service`: ELO projection, ranking history, ranking query API.
- `api-gateway`: ingress routing và cross-cutting HTTP concerns.

Không đẩy business logic của service này sang service khác chỉ vì tiện reuse.

### 6. Package Structure Inside A Service

Mỗi service hiện đang đi theo layered architecture gọn, không dùng clean architecture quá nặng.

Cấu trúc mặc định:

```text
controller/
configuration/
domain/entity/
domain/enums/
dto/request/
dto/response/
messaging/
repository/
service/
```

Quy tắc:

- `controller` chỉ nhận request, validate input HTTP, gọi service, trả response.
- `service` chứa business flow, transaction boundary, idempotency checks.
- `repository` chỉ truy cập persistence.
- `domain/entity` chứa JPA entities local của service.
- `messaging` chứa local event envelope/payload classes và handler/publisher logic của service.
- `configuration` chứa bean wiring, Jackson config, security config, route properties, v.v.

Không tạo thêm nhiều layer trung gian nếu chưa có nhu cầu thật.

### 7. Naming Rules

- Tên service module theo dạng kebab-case: `identity-service`, `ranking-service`.
- Tên class Java theo chuẩn Spring/Java thông thường.
- Controller đặt theo resource hoặc flow: `AuthController`, `RankingController`, `MatchController`.
- Service class đặt theo use case hoặc bounded behavior: `AuthenticationService`, `UserRegistrationHandler`, `MatchCompletedHandler`.
- Repository phải phản ánh aggregate đang lưu: `UserRepository`, `PlayerRankingRepository`.
- Event payload records đặt theo event contract: `UserRegisteredPayload`, `MatchCompletedPayload`.
- DTO request/response phải explicit, không dùng tên mơ hồ như `Data`, `Model`, `BaseResponse2`.

### 8. Identifiers And Entity Rules

- Public IDs dùng `UUID`.
- Không expose database-generated numeric IDs ra cross-service boundary.
- Nếu entity dùng `UUID`, tự gán trong `@PrePersist` hoặc constructor rõ ràng.
- Entity của service không được có foreign key hoặc JPA relation tới entity của service khác.
- Chỉ giữ ID của external resource, ví dụ `userId`, `accountId`, `sportId`.

Ví dụ đúng:

```java
UUID userId;
UUID accountId;
```

Ví dụ sai:

```java
@ManyToOne
private User user;
```

### 9. Database And Flyway Rules

- Mỗi service có thư mục migration riêng dưới `services/<service>/src/main/resources/db/migration/`.
- Version Flyway trong một service phải unique và tăng dần.
- Không sửa trực tiếp migration cũ đã được merge, trừ khi branch chưa được chia sẻ và team đồng ý.
- DDL phải phản ánh đúng state hiện tại của entity/service.
- Unique constraints phải encode business invariants quan trọng, ví dụ:
  - một subscription trên mỗi account
  - một role assignment duy nhất cho `(user_id, account_id, role_code)`
  - một outbox event duy nhất cho `(event_type, correlation_id)` khi flow yêu cầu idempotent publish

### 10. HTTP API Rules

- REST path phải nằm dưới `/api/v1/...`.
- Gateway route theo service responsibility, không route theo team hoặc package cũ.
- Controller trả DTO response, không trả JPA entity trực tiếp.
- Validation dùng annotation trên request DTO.
- Status code phải nhất quán:
  - `201 Created` cho create thành công
  - `200 OK` cho read/update non-create đơn giản
  - `400 Bad Request` cho input invalid
  - `401 Unauthorized` cho auth fail
  - `403 Forbidden` cho authenticated nhưng không được phép
  - `404 Not Found` khi resource không tồn tại
  - `409 Conflict` cho duplicate hoặc invariant conflict

### 11. Event And Messaging Rules

- Event contract chuẩn nằm ở `contracts/asyncapi/arenax-events.yaml`.
- Mỗi service tự định nghĩa local payload record cùng shape với contract, không import Java class từ service khác.
- Envelope chuẩn:
  - `eventId`
  - `eventType`
  - `eventVersion`
  - `occurredAt`
  - `correlationId`
  - `producer`
  - `payload`
- `correlationId` dùng để nối một business workflow xuyên services.
- `eventType` phải immutable sau khi public.
- Breaking change trong payload phải tạo version mới.
- Producer phải ghi business state và outbox cùng local transaction.
- Consumer phải được thiết kế idempotent.

### 12. Outbox And Idempotency Rules

- Service phát event phải có bảng `outbox_events` hoặc tên tương đương rõ nghĩa.
- Event chỉ được publish sau khi business state đã persist thành công.
- Với flow có thể nhận duplicate delivery, handler phải short-circuit an toàn.
- Không assume message broker delivers exactly once.
- Nếu service dùng `correlationId` để tránh duplicate publication, rule đó phải được ghi rõ trong service doc hoặc test.

### 13. Security Rules

- Chỉ `identity-service` được issue JWT.
- Downstream services validate token locally, không gọi đồng bộ lại Identity cho mỗi request.
- Token claims phải dựa trên authorization projection local trong Identity.
- Không hardcode secrets thật vào repo.
- Local defaults chỉ dùng cho development và phải thay được bằng config ngoài.
- Không persist raw refresh token nếu flow sau này hoàn thiện; lưu hash là target rule.

### 14. Testing Rules

- Mọi vertical slice mới phải bắt đầu bằng test fail trước.
- Ưu tiên integration-style tests với `@SpringBootTest` cho flow chính.
- Dùng `MockMvc` cho HTTP entrypoints.
- Dùng repository assertions để verify persistence side effects.
- Với event-driven flow, test phải kiểm tra cả state change lẫn outbox/history side effects.
- Với logic idempotency, luôn có test duplicate delivery.
- Với state transition, luôn có test happy path và test invariant violation quan trọng.

Mức tối thiểu cho một flow mới:

- một test create/success
- một test duplicate hoặc invalid state
- một test side effect quan trọng nhất

### 15. Error Handling Rules

- Không ném `RuntimeException` chung chung cho business rule chính.
- Dùng exception rõ nghĩa theo service khi flow đã đủ ổn định.
- HTTP error response phải ổn định về shape trong cùng service.
- Message lỗi phải rõ, ngắn, đúng nguyên nhân gần nhất.

### 16. Documentation Rules

- Khi thêm capability mới vào service, update file dưới `docs/services/<service>.md`.
- Khi thay đổi cross-service integration rule, update `docs/architecture/`.
- Khi thêm command dev/test mới, update `docs/development/` hoặc `README.md`.
- Không để docs mô tả monolith cũ hoặc flow không còn tồn tại.

### 17. Code Sharing Rules

Được chia sẻ:

- Gradle convention plugins
- dependency version catalog
- test utilities thuần kỹ thuật
- contract files `contracts/asyncapi`

Không được chia sẻ:

- JPA entities
- repositories
- business services
- event payload Java classes giữa services
- request/response DTOs giữa services
- Flyway migrations
- enum nghiệp vụ nếu điều đó ép services phải release cùng nhau

Nếu phân vân giữa reuse và duplicate, ưu tiên duplicate nhỏ để giữ boundary rõ.

### 18. Definition Of Done For A New Slice

Một vertical slice chỉ được xem là xong khi đủ tất cả điều kiện sau:

- test fail trước đã được viết
- implementation tối thiểu đã pass test
- migration tương ứng đã tồn tại
- response/request DTO rõ ràng
- business invariant chính đã được encode trong code hoặc DB constraint
- docs service đã được cập nhật nếu behavior thay đổi public hoặc integration-relevant
- `./gradlew test` vẫn xanh ở root

### 19. Things Explicitly Avoided In This Repo

- tạo lại monolith mới dưới root `src/`
- tạo `common` module chứa business logic lẫn lộn
- gọi đồng bộ giữa services cho mọi request chỉ vì tiện implement
- cross-service JPA relation
- publish event trực tiếp từ controller
- để test chỉ assert status code mà không assert state change
- để README/docs lệch khỏi source hiện tại

### 20. Change Policy

Nếu cần phá một convention trong tài liệu này, phải ghi rõ lý do trong PR hoặc update lại docs cùng lúc. Không silently drift khỏi convention.
