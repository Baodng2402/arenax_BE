# ArenaX — Convention Chung

Tài liệu này là điểm tham chiếu đầu tiên cho mọi developer trong project. Bao gồm: commit convention, branch naming, hướng dẫn tạo database, tổng quan architecture, coding convention, và checklist trước khi commit.

Các quy tắc chi tiết hơn theo từng chủ đề nằm ở:

- [`docs/querydsl-convention.md`](querydsl-convention.md) — Repository và QueryDSL
- [`docs/rbac-convention.md`](rbac-convention.md) — Phân quyền RBAC
- [`README.md`](../README.md) — Setup và commands

---

## 1. Commit Convention

### Format

```
<type>(<scope>): <mô tả ngắn>

[body tùy chọn — giải thích tại sao nếu cần]
```

### Types

| Type | Khi nào dùng |
| --- | --- |
| `feat` | Tính năng mới |
| `fix` | Sửa bug |
| `refactor` | Refactor code (không thêm tính năng, không sửa bug) |
| `docs` | Chỉ thay đổi tài liệu |
| `test` | Thêm hoặc sửa test |
| `chore` | Build, config, dependency (không ảnh hưởng logic) |
| `style` | Format code (không đổi logic) |

### Scope

Tên module hoặc layer: `auth`, `user`, `match`, `ranking`, `rbac`, `db`, `config`

### Ví dụ hợp lệ

```
feat(match): thêm API tạo trận đấu
fix(auth): sửa lỗi refresh token hết hạn sớm
refactor(user): tách UserServiceImpl thành các use-case nhỏ hơn
docs(rbac): cập nhật convention phân quyền
chore(db): thêm migration V6 tạo bảng courts
test(ranking): thêm unit test tính điểm ELO
```

### Quy tắc

- Mô tả ngắn ≤ 72 ký tự
- Không viết hoa chữ đầu sau dấu `:`
- Không dùng dấu chấm ở cuối
- Không dùng author tag kiểu `[Kane]` — dùng git author thay thế
- Giữ nhất quán ngôn ngữ (tiếng Anh hoặc tiếng Việt) trong một commit

---

## 2. Branch Naming Convention

### Format

```
<type>/<ten-tinh-nang>
```

Tên viết thường, dùng dấu `-` thay khoảng trắng (kebab-case).

### Prefixes

| Prefix | Khi nào dùng |
| --- | --- |
| `feat/` | Tính năng mới |
| `fix/` | Sửa bug |
| `refactor/` | Refactor |
| `docs/` | Cập nhật tài liệu |
| `hotfix/` | Fix khẩn cấp trên production |

### Ví dụ

```
feat/court-management
feat/match-joining
fix/refresh-token-expiry
refactor/user-service
docs/rbac-convention
hotfix/auth-null-pointer
```

### Quy tắc

- Chỉ dùng `feat/`, không dùng `feature/`
- Branch tạo từ `main`, merge về `main` qua Pull Request
- Xóa branch sau khi đã merge

---

## 3. Tạo Database Local

### Yêu cầu

- JDK 21
- Docker Desktop đang chạy

### Lần đầu setup

**Bước 1 — Tạo file `.env`:**

```bash
cp .env.example .env
```

**Bước 2 — Khởi động PostgreSQL:**

```bash
docker compose up -d
```

Kiểm tra container đã lên: `docker ps` → thấy `arenax-db` với trạng thái `healthy`.

**Bước 3 — Chạy ứng dụng:**

```bash
./gradlew bootRun
```

Flyway tự động apply toàn bộ migration trong `src/main/resources/db/migration/` khi app start. Không cần chạy SQL thủ công.

### Thông tin kết nối

| Trường | Giá trị |
| --- | --- |
| Host | `localhost` |
| Port | `5432` |
| Database | `arenax` |
| Username | `arenax` |
| Password | `arenax_dev_password` |

Dùng DBeaver, pgAdmin, hoặc psql để kết nối.

### Thêm migration mới

Tạo file mới trong `src/main/resources/db/migration/`:

```
V<số tiếp theo>__<mô_tả_bằng_snake_case>.sql
```

Ví dụ:

```
V6__create_courts_table.sql
V7__add_status_to_matches.sql
```

**Quy tắc migration:**

- Không bao giờ sửa file migration đã apply hoặc đã commit
- Mọi thay đổi schema phải qua file migration mới
- Entity Java phải khớp schema vì `ddl-auto=validate` — app sẽ fail fast nếu lệch

### Reset database

```bash
docker compose down -v   # Xóa container + volume
docker compose up -d     # Tạo lại từ đầu
./gradlew bootRun        # Flyway chạy lại từ V1
```

---

## 4. Architecture Overview

Project dùng **Hexagonal Architecture (Ports & Adapters)** kết hợp DDD.

```
┌──────────────────────────────────────────────────────┐
│  adapter/rest          (HTTP Controllers)             │  nhận request, validate, trả response
│  adapter/service       (Service Implementations)     │  business logic, transaction boundary
│  adapter/repository    (Spring Data JPA + QueryDSL)  │  data access
├──────────────────────────────────────────────────────┤
│  port/service          (Service Interfaces)           │  contract: controller → service
│  port/repository       (Repository Interfaces)        │  contract: service → repository
├──────────────────────────────────────────────────────┤
│  domain                (Entities, Enums)              │  pure Java, không phụ thuộc framework
├──────────────────────────────────────────────────────┤
│  infrastructure        (Security, Config, Converter)  │  JWT filter, exception handler, MapStruct
│  dto                   (Request / Response)           │  input/output shape của API
│  shared                (Pagination, Constants)        │  cross-cutting helpers
└──────────────────────────────────────────────────────┘
```

### Quy tắc quan trọng

- Controller không chứa business logic — chỉ nhận request, gọi service, trả response
- RBAC check ở service layer, không dùng `@PreAuthorize` ở controller
- `@Transactional` đặt trên service methods có sửa data
- Domain entity không import từ `dto` hay `adapter`

---

## 5. Coding Convention

### Đặt tên class

| Loại | Suffix | Ví dụ |
| --- | --- | --- |
| Controller | `*Controller` | `UserController` |
| Service interface | `*Service` | `UserService` |
| Service implementation | `*ServiceImpl` | `UserServiceImpl` |
| Repository | `*Repository` | `UserRepository` |
| Request DTO | `*Request` | `CreateUserRequest` |
| Response DTO | `*Response` | `UserResponse` |
| Exception | `*Exception` | `UserNotFoundException` |
| Error code | `ARENAX.<DOMAIN>.<NUMBER>` | `ARENAX.USR.001` |

### Dependency Injection

Dùng constructor injection qua Lombok `@RequiredArgsConstructor`. Không dùng `@Autowired` field injection.

```java
// Đúng
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final UserMapper userMapper;
}
```

### DTO

Dùng `record` cho DTO đơn giản. Dùng `class` khi cần builder hoặc kế thừa.

```java
public record CreateUserRequest(
    @NotBlank String email,
    @NotBlank String password) {}
```

### Error codes

Định nghĩa trong `infrastructure/exception/ErrorCode.java`. Không hardcode chuỗi lỗi trong service hay controller.

```
ARENAX.AUTH.001  — lỗi xác thực
ARENAX.USR.001   — lỗi liên quan user
ARENAX.VAL.001   — lỗi validation
ARENAX.SYS.999   — lỗi hệ thống không xác định
```

### Pagination

Xem chi tiết tại `docs/querydsl-convention.md`. Tóm tắt:

- Controller nhận `@ModelAttribute BasePaginationRequest request`
- Service trả `BasePaginationResponse<T>`
- Repository dùng `PaginationHelper.setPage(request)` để tạo `Pageable`

### RBAC

Xem chi tiết tại `docs/rbac-convention.md`. Tóm tắt:

- Permission naming: `<MODULE>_<ACTION>` (ví dụ: `USER_VIEW`, `COURT_MANAGE`)
- Gọi `rbacAuthorizationService.requirePermission(...)` ở đầu service method
- Không cache role/permission trong JWT

---

## 6. Checklist Trước Khi Commit

```
[ ] Chạy ./gradlew spotlessApply để format code
[ ] Chạy ./gradlew spotlessCheck compileJava test để verify toàn bộ
[ ] Migration file mới đặt tên đúng: V<n>__<mô_tả>.sql
[ ] Không commit file trong build/ hoặc file .env
[ ] Commit message đúng format: type(scope): mô tả
[ ] Branch name đúng format: type/feature-name
[ ] Không còn unused import hoặc TODO chưa xử lý
[ ] Nếu có endpoint mới cần bảo vệ: đã thêm RBAC check ở service
[ ] Nếu thêm error mới: đã định nghĩa trong ErrorCode enum
```
