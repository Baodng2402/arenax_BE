> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# 07. Architecture Decisions

Tài liệu này giải thích ngắn gọn vì sao repo đang ở trạng thái hiện tại.

## 1. Vì Sao Chuyển Sang Multi-Project Monorepo

Mục tiêu của lần restructure không phải chỉ là chia thư mục.

Mục tiêu là:

- ép boundary giữa các service rõ hơn
- để mỗi service own build, entity, migration và test riêng
- tránh việc monolith cũ tiếp tục kéo mọi thứ vào cùng một persistence model

Monorepo vẫn được giữ để:

- dễ refactor đồng thời nhiều service khi architecture còn đang ổn định dần
- giữ contract, docs và conventions ở một chỗ
- đơn giản hóa việc học và review toàn hệ thống

## 2. Vì Sao Không Còn Root App Để Chạy

Trước đây root project là monolith nên có một app entrypoint chung.

Bây giờ root chỉ còn là Gradle aggregator.

Điều đó có nghĩa là:

- `./gradlew test` ở root vẫn hợp lệ
- `./gradlew projects` ở root vẫn hợp lệ
- nhưng root không còn là Spring Boot app để `bootRun`

Muốn chạy app, phải chạy từng subproject cụ thể như:

```bash
./gradlew :services:api-gateway:bootRun
./gradlew :services:identity-service:bootRun
```

Đây là hành vi đúng theo kiến trúc mới, không phải bug của Gradle.

## 3. Vì Sao Chọn Database-Per-Service

Mỗi service đang own migration và schema riêng để:

- tránh shared schema coupling
- giữ invariant gần business owner của nó
- giúp sau này deploy/runtime split đúng nghĩa microservices

Trade-off là local run phức tạp hơn monolith vì phải có datasource cho từng service persistence.

## 4. Vì Sao Không Share Entity Hoặc DTO Java Giữa Services

Repo cố ý không share:

- JPA entities
- repositories
- business services
- Java event payload classes giữa services

Lý do là nếu share các lớp đó thì boundary chỉ tồn tại trên giấy, release cadence sẽ bị kéo dính trở lại.

Thay vào đó:

- contract được share ở mức file AsyncAPI
- mỗi service tự có local payload class cùng shape với contract

## 5. Vì Sao Chọn Outbox Trước Khi Wiring RabbitMQ Thật

Mục tiêu của phase hiện tại là ổn định source architecture trước.

Outbox đã được thêm trước vì nó là phần nghiệp vụ và dữ liệu quan trọng hơn broker runtime.

Điều này cho phép repo chốt sớm:

- event nào tồn tại
- producer nào chịu trách nhiệm phát event
- side effect nào phải cùng transaction với business state
- idempotency rule nào cần test

Quyết định này đã được hiện thực: outbox relay + listener adapter (RabbitMQ) được thêm mà không phải thiết kế lại business flow từ đầu.

## 6. Vì Sao Identity Giữ Authorization Projection Local

Identity không nên gọi đồng bộ sang Access mỗi lần login hoặc mỗi lần validate token.

Projection local được dùng để:

- materialize `roles` và `permissions`
- issue JWT với đủ claims
- cho phép downstream validate token locally

Đây là trade-off có chủ đích để giảm coupling runtime.

## 7. Vì Sao Gateway Dùng Static URI Thay Vì Eureka

Hiện tại gateway route bằng URI tĩnh vì:

- source architecture đang là ưu tiên số 1
- local development dễ hiểu hơn
- chưa có nhiều runtime instance cần discovery động

Eureka không bị loại bỏ vĩnh viễn, nhưng chưa cần ở phase này.

## 8. Vì Sao Repo Hiện Chưa One-Command Start Full Stack

Repo đã được đẩy nhanh về phía đúng boundary và testable slices, nhưng chưa hoàn thiện phần platform runtime.

Hiện chưa có:

- Docker Compose mới cho toàn stack
- RabbitMQ wiring đầy đủ
- datasource local profile mặc định cho mọi service
- observability đầy đủ

Nói cách khác, repo hiện mạnh ở source architecture và test slices, chưa mạnh ở developer runtime convenience.

Đó là trạng thái có chủ đích của phase hiện tại.
