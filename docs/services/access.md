> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# Access Service

> **Merged into Identity Service.** The `access-service` module no longer exists.

Its responsibilities moved into `identity-service`:

- define permissions and roles (`permissions`, `roles`, `role_permissions`)
- assign roles per account (`role_assignments`)
- include `roles` / `permissions` claims in issued JWTs

See [Identity Service](identity.md) for the current state.