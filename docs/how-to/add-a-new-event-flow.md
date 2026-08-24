# How To Add A New Event Flow

File này dùng khi bạn muốn nối một flow mới giữa services bằng event.

## Khi Nào Nên Chọn Event

Ưu tiên event nếu:

- caller không cần response ngay trong request hiện tại
- flow là propagate state hoặc trigger side effect sang service khác
- bạn muốn giữ service boundaries lỏng hơn HTTP sync orchestration

Nếu caller cần câu trả lời ngay, xem `add-an-internal-http-call.md`.

## Standard Flow

### 1. Chốt producer và consumer

Trả lời rõ:

- service nào là source of truth?
- event business meaning là gì?
- service nào consume event này?
- duplicate delivery thì consumer phải xử lý ra sao?

### 2. Cập nhật contract trước

Update `docs/contracts/asyncapi/arenax-events.yaml`:

- thêm channel/message/schema mới
- chọn `eventType` stable, semantic, versioned
- nếu là breaking change thì tạo version mới thay vì sửa im lặng event cũ

Thêm example JSON dưới `docs/contracts/asyncapi/examples/` nếu event đó sẽ được dùng thật.

### 3. Producer: tạo payload local và ghi outbox cùng transaction

Trong producer service:

- tạo local payload class/record cùng shape với contract
- business service persist state chính
- trong cùng transaction đó, ghi row vào `outbox_events`

Không publish trực tiếp từ controller.

## 4. Consumer: tạo listener và handler idempotent

Trong consumer service:

- tạo `@RabbitListener` adapter
- deserialize `EventEnvelope<YourPayload>`
- delegate sang handler/service local
- handler phải idempotent

Idempotency có thể dựa trên business key như `correlationId`, `accountId`, `matchId`, hoặc invariant persistence local.

## 5. Viết test tối thiểu

Ít nhất nên có:

- một test happy path
- một test duplicate delivery / idempotency
- một test assert side effect quan trọng nhất

Với producer, test phải chứng minh business state và outbox row được tạo đúng.
Với consumer, test phải chứng minh duplicate message không làm apply side effect hai lần.

## Naming Rules

- `eventType` phải immutable sau khi public
- payload class đặt theo contract, ví dụ `UserRegisteredPayload`
- không share payload Java class giữa services
- `correlationId` nên là business key giúp trace workflow xuyên services

## Review Checklist

Trước khi merge:

- contract AsyncAPI đã update chưa?
- example JSON đã có chưa?
- producer ghi outbox trong cùng local transaction chưa?
- consumer đã idempotent chưa?
- test duplicate delivery đã có chưa?
- service docs liên quan đã update chưa?

## Related Docs

- `../contracts/asyncapi/arenax-events.yaml`
- `../architecture/conventions.md`
- `../architecture/event-conventions.md`
- `../development/testing.md`
