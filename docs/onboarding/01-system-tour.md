> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# 01. System Tour

File này không lặp lại toàn bộ `docs/overview.md`. Mục tiêu của nó là chỉ cho bạn cách nhìn repo này cho đúng trước khi đi sâu vào code.

## The Right Mental Model

- Đây là **multi-service monorepo**, không phải monolith tách package.
- Mỗi service own database schema, migration, entity, repository, test, và business flow của mình.
- Cross-service communication mặc định là **event-first**; HTTP nội bộ chỉ dùng khi cần response ngay.
- `libs/` chỉ dành cho shared technical implementation ổn định; không dùng để gom business code dùng chung.
- `docs/contracts/` là contract/spec artifacts, không phải runtime module.

## What To Look At First

Nếu bạn muốn hiểu repo theo đúng thứ tự, đi như này:

1. `docs/overview.md` — repo map, service map, integration model.
2. `docs/architecture/README.md` rồi `conventions.md` — luật chơi khi thêm code mới.
3. `02-core-flows.md` — các flow business chạy xuyên services.
4. `04-data-and-integration-map.md` — ai own data gì, produce/consume event gì.

## What This Repo Optimizes For

Repo hiện ưu tiên:

- boundary rõ
- event contract rõ
- source architecture rõ
- vertical slice có test

Repo chưa tối ưu cho:

- production deployment hoàn chỉnh
- observability đầy đủ
- full platform automation

Điều này quan trọng vì khi bạn thấy chỗ nào còn thiếu runtime/platform, đó không nhất thiết là bug kiến trúc; nhiều phần đơn giản là chưa đến giai đoạn làm tiếp.

## After This File

- Đọc `02-core-flows.md` nếu bạn cần hiểu flow business.
- Đọc `03-domain-glossary.md` nếu bạn chưa quen domain terms.
- Đọc `../how-to/` nếu bạn sắp bắt đầu implement.
