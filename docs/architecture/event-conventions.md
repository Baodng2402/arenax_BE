> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# ArenaX Event Conventions

File này là checklist ngắn cho event-driven integration. Rule tổng quát vẫn lấy từ `conventions.md`, nhất là mục 11 và 12.

## Canonical Contract

- Contract nguồn nằm ở `../contracts/asyncapi/arenax-events.yaml`.
- Khi thêm event mới, update contract trước rồi mới code producer/consumer.
- Payload Java class phải local trong từng service, không share giữa services.

## Envelope

All integration events use this envelope:

```json
{
  "eventId": "uuid",
  "eventType": "identity.user.registered.v2",
  "eventVersion": 2,
  "occurredAt": "2026-07-13T10:00:00Z",
  "correlationId": "uuid",
  "producer": "identity-service",
  "payload": {}
}
```

## Rules

- `eventId` is globally unique and used for idempotency.
- `correlationId` ties one business workflow across services.
- `eventType` is immutable once published.
- Breaking payload changes require a new event version.
- Producers write to an outbox in the same local transaction as business state.
- Consumers store handled `eventId` values to avoid duplicate processing.

## Before You Merge

- AsyncAPI contract đã update chưa?
- Example JSON dưới `../contracts/asyncapi/examples/` đã có chưa?
- Producer đã ghi business state và outbox cùng transaction chưa?
- Consumer đã idempotent chưa?
- Test duplicate delivery đã có chưa?

Nếu bạn cần quy trình implement đầy đủ, đọc thêm `../how-to/add-a-new-event-flow.md`.
