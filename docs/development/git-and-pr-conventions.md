# Git And PR Conventions

## Branch Naming

- Feature lớn: `feat/<topic>`
- Bug fix: `fix/<topic>`
- Refactor: `refactor/<topic>`
- Docs only: `docs/<topic>`
- Chore/build tooling: `chore/<topic>`

Ví dụ:

- `feat/full-microservices-migration`
- `feat/competition-complete-match`
- `fix/identity-login-projection`

## Commit Style

Ưu tiên commit message ngắn, mô tả đúng intent:

- `build: convert repo to multi-project services`
- `feat(identity): register provisioning users`
- `feat(tenant): publish personal account events`
- `test(ranking): cover duplicate match delivery`
- `docs: rewrite microservice repository guides`

Không dùng commit message mơ hồ như:

- `update code`
- `fix stuff`
- `done`

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
- Khi feature còn dở dang nhưng cần backup branch, phải ghi rõ trạng thái trong PR hoặc trao đổi team.
- Không force-push branch shared trừ khi cả team đã thống nhất.
