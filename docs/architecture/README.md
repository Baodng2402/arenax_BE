> **Architecture entrypoint** - đọc file này khi bạn cần rulebook và reading path cho boundary, messaging, internal HTTP, hoặc bootstrap service mới.

# Architecture Guide

`docs/architecture/` là nơi giữ các rule và reference về kiến trúc repo. Đây không phải chỗ để đọc toàn bộ repo từ đầu; hãy bắt đầu từ `README.md` rồi `docs/overview.md` trước.

## Start Here

1. `README.md` để hiểu repo này phục vụ domain gì.
2. `docs/overview.md` để nắm canonical architecture và service map.
3. `docs/architecture/conventions.md` trước khi sửa code hoặc thêm flow mới.

## File Map

- `conventions.md`: rulebook chính của repo. Khi có mâu thuẫn trong `docs/architecture/`, lấy file này làm chuẩn.
- `service-boundaries.md`: bản rút gọn về ownership giữa services.
- `openfeign-conventions.md`: rule riêng cho internal synchronous HTTP.
- `event-conventions.md`: checklist ngắn cho envelope, versioning, idempotency của event-driven integration.
- `internal-endpoint-template.md`: skeleton ngắn cho `/internal/v1/**` endpoint.
- `service-template.md`: bootstrap template khi thật sự tạo service mới.

## Reading Paths

- Nếu bạn sắp thêm một flow mới: đọc `conventions.md` trước, sau đó sang `../how-to/`.
- Nếu bạn sắp thêm internal HTTP call: đọc `openfeign-conventions.md` rồi `../how-to/add-an-internal-http-call.md`.
- Nếu bạn sắp tạo service mới: đọc `service-boundaries.md`, `service-template.md`, rồi `../development/local-development.md`.

## Boundaries For This Folder

- Giữ rule tổng quát ở `conventions.md`.
- Giữ template/reference ngắn ở các file còn lại.
- Không lặp lại toàn bộ repo overview ở đây; phần đó thuộc `docs/overview.md`.
