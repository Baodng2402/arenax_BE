> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# ArenaX Onboarding Guide

Chào mừng bạn đến với ArenaX Backend. Folder `docs/onboarding/` không phải canonical source of truth; nó là lộ trình đọc để giúp developer mới vào repo nhanh hơn.

## Start Here

1. Đọc `README.md` rồi `docs/overview.md` để nắm repo map và integration model.
2. Đọc `docs/architecture/conventions.md` để biết boundary và rule trước khi sửa code.
3. Chọn đường đọc tiếp theo theo mục tiêu hiện tại của bạn.

## Reading Paths

### Nếu bạn mới vào repo

- `01-system-tour.md` — cách nhìn repo và boundary cho đúng
- `02-core-flows.md` — các flow nghiệp vụ chính
- `03-domain-glossary.md` — các thuật ngữ domain quan trọng
- `04-data-and-integration-map.md` — service nào own data gì, consume/produce gì

### Nếu bạn muốn bắt đầu implement

- `../how-to/add-a-shared-lib.md`
- `../how-to/add-a-new-event-flow.md`
- `../how-to/add-an-internal-http-call.md`
- `../development/README.md`

### Nếu bạn muốn biết trạng thái repo

- `05-current-status-and-gaps.md` — snapshot tổng hợp
- `08-remaining-work-tracker.md` — checklist hành động còn mở

## Notes

- `docs/overview.md` là overview canonical; nếu onboarding doc nào lệch thì lấy `docs/overview.md` làm chuẩn.
- `docs/contracts/` chứa contract/spec artifacts; không phải runtime module.
- `docs/services/` là deep-dive theo từng service khi bạn đã biết mình sẽ chạm vào module nào.
