# Git And PR Conventions

## Branch Naming

- Dùng tên ngắn, rõ mục tiêu, viết thường, nối bằng dấu gạch ngang nếu cần.
- Prefix chuẩn:
  - Feature: `feat/<topic>`
  - Bug fix: `fix/<topic>`
  - Refactor: `refactor/<topic>`
  - Docs only: `docs/<topic>`
  - Chore/build tooling: `chore/<topic>`
- Chọn topic theo business slice hoặc kỹ thuật cụ thể, không đặt tên mơ hồ như `update`, `misc`, `temp`.

Ví dụ:

- `feat/full-microservices-migration`
- `feat/competition-complete-match`
- `fix/identity-login-projection`
- `docs/git-and-pr-conventions`

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

- mục tiêu thay đổi
- service hoặc module bị ảnh hưởng
- migration/event/API nào bị tác động
- lệnh test đã chạy
- ghi chú boundary rule nào cần reviewer chú ý

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
