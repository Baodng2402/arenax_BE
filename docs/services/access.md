> **Historical tombstone** — `access-service` không còn tồn tại như module riêng. File này chỉ giữ đường dẫn cũ trỏ về service hiện tại.

# Access Service

> **Merged into Identity Service.** The `access-service` module no longer exists.

Its responsibilities moved into `identity-service`:

- define permissions and roles (`permissions`, `roles`, `role_permissions`)
- assign roles per account (`role_assignments`)
- include `roles` / `permissions` claims in issued JWTs

See [Identity Service](identity.md) for the current state.

This file is kept only as a historical tombstone so older discussions or branch notes still resolve to the current module.
