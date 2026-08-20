> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# Remaining Work Tracker

Trạng thái triển khai thực tế so với plan platform-core. Cập nhật khi có thay đổi; chi tiết theo từng mục xem `05-current-status-and-gaps.md`.

## Chưa OK — cần implement tiếp

### Messaging / Async runtime
- [ ] **Email-sender consumer** cho `identity.user.verification-requested.v1` và `identity.user.password-reset-requested.v1` — hiện chưa có consumer nào; cần tích hợp SMTP provider (rõ domain/credential trước khi làm).
- [ ] **Consumer cho subscription events** (`subscription.activated.v1`, `subscription.changed.v1`, `subscription.cancelled.v1`) — hiện chưa service nào đăng ký; chỉ để dành cho notification/billing sau.
- [ ] **Retry / DLQ**: chưa có hàng đợi retry/dead-letter ở tầng adapter; consumer đang dựa vào idempotency của handler (correlationId / matchId).
- [ ] **Inbox pattern** (handled-message tracking) ở adapter layer — hiện chỉ dedupe qua handler + bảng business.

### Account / Identity
- [ ] **Xác thực JWT `account_id`**: identity chưa validate `accountId` có thuộc về user thật qua tenant membership (`role_assignments`/membership lookup) trước khi ghi vào claim.
- [ ] **Email thật khi reset password**: chỉ có event; chưa có channel delivery thực (SMTP).

### Gateway / API surface
- [ ] **Routes cho competition-service và ranking-service** chưa được bật trong gateway (chưa có tests tương ứng).
- [ ] **OpenAPI docs còn thiếu** cho competition và ranking slices.

### Security
- [ ] **Service-to-service auth**: hiện chỉ tin trusted headers từ gateway; chưa có mTLS / client-credentials cho internal calls.

### Runtime hạ tầng
- [ ] Chạy local cần `docker compose up -d rabbitmq` (broker chưa nằm trong mặc định của compose profile base).
- [ ] **discovery-server** chỉ có smoke test `contextLoads()` — chưa có integration test với Eureka client.
- [ ] **Redis** đang trong `compose.yaml` nhưng chưa service nào dùng — xác nhận mục đích (cache? session?) hoặc bỏ.
- [ ] **CI/CD + observability** chưa có (đã ghi ở `01-system-tour.md`).

## Ghi chú vận hành

- Khi thêm event mới: cập nhật `contracts/asyncapi/arenax-events.yaml` + example JSON, rồi thêm consumer (nếu có) — đừng để event "mồ côi".
- Test mới về messaging nên dùng `@MockitoBean RabbitTemplate` + tắt relay/listener trong `src/test/resources/application.yaml` (xem pattern hiện có ở các service).