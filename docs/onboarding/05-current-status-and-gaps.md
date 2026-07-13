# 05. Current Status And Gaps

## Những Gì Đã Có

- Monolith cũ đã được thay bằng microservice monorepo theo boundary rõ ràng.
- Mỗi service có module Gradle riêng.
- Mỗi service có entity/repository/migration riêng.
- Các flow chính đã có integration tests.
- AsyncAPI contract đã có.
- Gateway routing cơ bản đã có.
- Identity đã issue JWT bằng RSA.
- Onboarding flow và ranking flow đã có bản source-level chạy trong test.

## Những Gì Chưa Có

### Messaging Runtime

Chưa có RabbitMQ wiring thật sự cho:

- publisher adapter
- listener adapter
- broker topology
- retry / dead-letter handling
- handled-message inbox persistence ở adapter layer

Hiện tại event handling mới dừng ở service-layer flow và outbox persistence.

### Auth Runtime Hoàn Chỉnh

Chưa hoàn thiện:

- refresh token lifecycle đúng nghĩa
- hashed refresh token persistence rule
- gateway security policy hoàn chỉnh cho mọi route
- service-to-service auth

### Infrastructure

Chưa có:

- Docker Compose cho full stack microservices
- PostgreSQL containers riêng cho local distributed runtime
- CI/CD
- VPS deployment
- centralized logging
- tracing/metrics/alerts
- circuit breaker/resilience policy

### API Maturity

Chưa có hoặc chưa hoàn thiện đầy đủ:

- OpenAPI docs hoàn chỉnh
- uniform production-grade error model cho toàn repo
- full authorization checks cho mọi business endpoint

## Điều Này Có Nghĩa Gì Với Người Mới Join

Nếu bạn mới vào repo, hãy hiểu rằng:

- source architecture đã rõ
- implementation slice đầu tiên đã có
- nhưng runtime platform vẫn đang ở phase tiếp theo

Nói ngắn gọn: đây là một nền microservice đúng boundary, chưa phải production platform hoàn chỉnh.

## Cách Đọc “Tiến Độ” Cho Đúng

- Nếu bạn muốn hiểu business structure: repo này đã khá rõ.
- Nếu bạn muốn chạy full distributed stack bằng một lệnh: chưa xong.
- Nếu bạn muốn thêm flow mới theo pattern hiện tại: đã có đủ template và conventions.
