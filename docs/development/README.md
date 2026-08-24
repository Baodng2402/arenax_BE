> **Development entrypoint** — nếu bạn chuẩn bị chạy repo hoặc bắt đầu code, đọc file này trước. Sau đó mới đi sang guide chi tiết phù hợp.

# Development Guide

`docs/development/` là nhóm tài liệu dành cho việc chạy repo, test, và làm việc hằng ngày. Canonical repo/domain context vẫn nằm ở `README.md` và `docs/overview.md`; thư mục này chỉ trả lời câu hỏi: "muốn code hoặc chạy local thì làm gì tiếp theo?"

## Start Here

1. Đọc `README.md`
2. Đọc `docs/overview.md`
3. Đọc `docs/architecture/conventions.md` nếu bạn sắp sửa code
4. Quay lại `docs/development/local-development.md` để chạy local

## Reading Paths

Nếu bạn muốn chạy repo local lần đầu:

- `local-development.md`
- `running-the-stack.md`
- `running-services.md`

Nếu bạn muốn chạy bằng IntelliJ:

- `local-development.md`
- `intellij-setup.md`

Nếu bạn muốn verify thay đổi trước khi push:

- `testing.md`
- `git-and-pr-conventions.md`

## What Each File Is For

- `local-development.md`: quickstart chuẩn để dựng infra, chạy test, chạy service
- `running-the-stack.md`: chi tiết về local runtime stack và smoke checks
- `running-services.md`: chi tiết về chạy từng service bằng Gradle hoặc IntelliJ
- `intellij-setup.md`: hướng dẫn IntelliJ chi tiết hơn
- `testing.md`: test expectations và command cơ bản
- `git-and-pr-conventions.md`: branch, commit, PR workflow của repo

## Current Reality

- root project là Gradle multi-project root, không phải app runnable
- service được chạy qua subproject task như `:services:identity-service:bootRun`
- persistence services cần profile `local`
- repo hiện không còn helper script `bin/run-service` hay `bin/run-local-stack`; dùng Gradle CLI và `docker compose` trực tiếp
