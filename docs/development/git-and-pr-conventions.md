> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# Git And PR Conventions

## Branch Naming

Format chuẩn: `<type>/<JIRA-KEY>-<topic>`

- Dùng tên ngắn, rõ mục tiêu, viết thường, nối bằng dấu gạch ngang nếu cần.
- Luôn gắn Jira issue key (`KAN-<số>`) ngay sau prefix — đây là thứ Jira dùng để link PR với ticket (xem `Jira Integration` bên dưới).
- Prefix chuẩn:
  - Feature: `feat/<JIRA-KEY>-<topic>`
  - Bug fix: `fix/<JIRA-KEY>-<topic>`
  - Refactor: `refactor/<JIRA-KEY>-<topic>`
  - Docs only: `docs/<JIRA-KEY>-<topic>`
  - Chore/build tooling: `chore/<JIRA-KEY>-<topic>`
- Chọn topic theo business slice hoặc kỹ thuật cụ thể, không đặt tên mơ hồ như `update`, `misc`, `temp`.

Ví dụ:

- `feat/KAN-7-email-sender-consumer`
- `feat/KAN-11-gateway-ranking-route`
- `fix/KAN-10-validate-account-id`
- `docs/KAN-17-pr-convention`

Nếu công việc thực sự không có ticket (hotfix gấp, chore vặt), vẫn giữ format cũ `<type>/<topic>` nhưng ghi rõ lý do trong PR — trường hợp này ticket sẽ không tự chuyển trạng thái.

## Branch Workflow

- Mỗi branch nên phục vụ đúng một intent chính.
- Nếu thay đổi lớn, tách thành nhiều branch thay vì nhồi tất cả vào một branch dài hạn.
- Nếu cần backup work đang dang dở, giữ branch đó riêng và ghi rõ trạng thái trong PR hoặc trao đổi team.
- Không force-push branch đã được người khác dùng chung, trừ khi team đã thống nhất.

## Commit Style

- Ưu tiên format hybrid:
  - `type(scope): subject` cho code change rõ ràng
  - `docs: ...` cho tài liệu
  - `chore: ...` cho thay đổi tooling hoặc dọn dẹp
- Scope nên là service hoặc area có thật: `identity`, `tenant`, `ranking`, `gateway`, `docs`, `build`.
- Subject ngắn, chủ động, viết thường, mô tả điều đã làm.

Ví dụ tốt:

- `build: convert repo to multi-project services`
- `feat(identity): register provisioning users`
- `feat(tenant): publish personal account events`
- `test(ranking): cover duplicate match delivery`
- `docs: rewrite microservice repository guides`

Khi sửa nhỏ hoặc docs-only, có thể dùng câu ngắn hơn nếu vẫn rõ nghĩa, nhưng vẫn nên giữ cùng tinh thần trên.

Nếu branch đã mang Jira key thì commit message không bắt buộc lặp lại key. Chỉ thêm key vào commit (`feat(identity): KAN-10 validate account id`) khi branch không có key hoặc khi một branch phục vụ nhiều ticket.

Không dùng commit message mơ hồ như:

- `update code`
- `fix stuff`
- `done`

## Commit Workflow

- Commit nhỏ, mỗi commit giữ một ý nghĩa riêng.
- Nếu một commit bắt đầu khó mô tả trong một câu, tách nó ra.
- Trước khi commit, tự hỏi: nếu revert commit này thì có làm hỏng phần khác không?
- Nếu câu trả lời là có, commit đang quá lớn.

## PR Expectations

Mỗi PR nên có:

- Jira issue key trong title, ví dụ `KAN-17: bổ sung convention gắn issue key vào branch/PR`
- mục tiêu thay đổi
- service hoặc module bị ảnh hưởng
- migration/event/API nào bị tác động
- lệnh test đã chạy
- ghi chú boundary rule nào cần reviewer chú ý

## Jira Integration

Repo đã kết nối với Jira project `KAN` (site `arenaxkb.atlassian.net`) qua app GitHub for Jira. Jira nhận diện ticket bằng cách quét issue key trong **branch name**, **PR title**, và **commit message**.

Automation flow đang bật:

| Sự kiện GitHub | Kết quả trên Jira |
|---|---|
| Mở pull request | Ticket chuyển sang `In Review` |

Hệ quả thực tế:

- Không có issue key ở đâu cả → PR không link được ticket, trạng thái không tự đổi, phải kéo tay trên board.
- Đặt key ở branch name là chắc nhất vì Jira thấy ngay từ lúc push, không cần đợi mở PR.
- Một PR có thể tham chiếu nhiều ticket bằng cách nêu các key trong commit message, nhưng nên giữ một PR một ticket cho dễ trace.

## Review Checklist

Trước khi merge, reviewer nên kiểm tra:

- code mới có nằm đúng service không
- có vi phạm cross-service dependency không
- migration có đúng service owner không
- event payload có khớp contract không
- đã có test idempotency hoặc state transition quan trọng chưa
- docs có cần update không

## Push Policy

- Không push code chưa qua test local tối thiểu của phần vừa sửa.
- Khi branch còn dở dang nhưng cần backup, ghi rõ trạng thái và phạm vi còn thiếu.
