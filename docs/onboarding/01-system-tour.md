> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# 01. System Tour

## Mục Tiêu Của Repo Này

ArenaX backend hiện là một microservice monorepo theo hướng source-first.

Mục tiêu hiện tại là:

- tách rõ business boundary
- giữ database ownership riêng cho từng service
- chuẩn hóa event contract và outbox pattern
- xây được các vertical slice có test trước khi làm hạ tầng runtime hoàn chỉnh

Repo này chưa cố gắng hoàn thiện deployment production ngay lập tức.

## Bức Tranh Tổng Quan

Các service hiện có:

- `api-gateway`: cửa vào HTTP chung
- `identity-service`: đăng ký, đăng nhập, token, user identifiers/email, RBAC (roles, permissions, role assignments theo account)
- `tenant-service`: account và membership
- `subscription-service`: subscription theo account
- `competition-service`: sport, match, participant, kết quả trận
- `ranking-service`: ELO projection và query ranking

## Monorepo Layout

```text
build-logic/                 Gradle conventions dùng chung
contracts/asyncapi/          event contracts và examples
docs/                        kiến trúc, onboarding, service notes
gradle/libs.versions.toml    version catalog
services/
├── api-gateway/
├── identity-service/
├── tenant-service/
├── subscription-service/
├── competition-service/
└── ranking-service/
```

## Nguyên Tắc Kiến Trúc Chính

- Mỗi service own schema, migration, entity, repository và test riêng.
- Không share business entity hoặc repository giữa services.
- Cross-service reference dùng `UUID`, không dùng JPA relation xuyên service.
- HTTP chỉ dùng khi thật sự cần câu trả lời ngay.
- Hướng tích hợp chính là event-driven với AsyncAPI contract.
- Producer lưu outbox cùng local transaction.
- Consumer phải idempotent.

## Cách Nhìn Repo Này Cho Đúng

Đây chưa phải một hệ thống distributed hoàn chỉnh đang chạy full stack.

Đây là một codebase đã:

- chốt boundary
- chốt shape của event
- có service-local tests cho các flow chính
- có gateway routing cơ bản

Nhưng vẫn còn thiếu phần runtime integration như CI/CD và observability. Docker Compose hiện có Postgres, Redis, discovery-server và RabbitMQ.

## Nên Đọc Gì Sau File Này

- Đọc `02-core-flows.md` để hiểu các luồng business chính.
- Đọc `03-domain-glossary.md` để không bị lẫn khái niệm.
- Đọc `04-data-and-integration-map.md` để biết service nào own cái gì.
