# ArenaX RBAC Convention

Tài liệu này mô tả convention dùng RBAC trong ArenaX để dev khác đọc là biết cách đặt quyền, seed quyền, và gọi check quyền trong code.

## 1. Mental Model

ArenaX dùng dynamic RBAC theo mô hình:

```text
User -> UserRoleAssignment -> Role -> Permission
```

- `User`: identity/profile/login của người dùng.
- `Account`: workspace/tenant/subscription container.
- `UserRoleAssignment`: gán role cho user trong một account cụ thể.
- `Role`: nhóm quyền, ví dụ `ADMIN`, `MANAGER`, `STAFF`, `USER`.
- `Permission`: quyền theo module/action, ví dụ `USER_VIEW`, `COURT_MANAGE`.

Không check quyền trực tiếp trong controller bằng `@PreAuthorize`. Controller chỉ nhận request và gọi service. Service tự gọi `RbacAuthorizationService` để kiểm tra quyền.

## 2. Permission Naming

Permission code luôn dùng format:

```text
<MODULE>_<ACTION>
```

Ví dụ:

```text
USER_VIEW
USER_CREATE
USER_UPDATE
USER_DELETE
COURT_VIEW
COURT_MANAGE
RBAC_VIEW
RBAC_MANAGE
```

Rules:

- Dùng uppercase.
- Dùng underscore `_`, không dùng space hoặc hyphen.
- `MODULE` là domain/module nghiệp vụ.
- `ACTION` là hành động được phép làm.
- Không đặt permission bắt đầu bằng `ROLE_`.

Lý do không dùng `ROLE_*` cho permission: Spring Security convention dùng prefix `ROLE_` cho role authority. Permission như `ROLE_MANAGE` sẽ bị hiểu nhầm là role, gây conflict với dynamic RBAC.

## 3. Standard Actions

Dùng các action chuẩn sau khi có thể:

```text
VIEW
CREATE
UPDATE
DELETE
MANAGE
```

Ý nghĩa:

- `VIEW`: xem/list/detail.
- `CREATE`: tạo mới.
- `UPDATE`: cập nhật.
- `DELETE`: xóa.
- `MANAGE`: quyền quản trị module, được xem như wildcard cho module đó.

Ví dụ user có `COURT_MANAGE` thì được phép thực hiện các action `COURT_VIEW`, `COURT_CREATE`, `COURT_UPDATE`, `COURT_DELETE` nếu service check qua `RbacAuthorizationService`.

## 4. Module Naming

Module hiện tại đang seed trong `V4__add_dynamic_rbac.sql`:

```text
ACCOUNT
USER
RBAC
COURT
BOOKING
SUBSCRIPTION
REPORT
SYSTEM_CONFIG
```

Khi thêm module mới:

1. Chọn tên module rõ nghĩa, uppercase, ví dụ `PAYMENT`, `NOTIFICATION`.
2. Thêm permission vào migration mới, không sửa migration đã chạy ở production.
3. Gán permission đó cho role phù hợp qua seed hoặc admin RBAC API.
4. Service nghiệp vụ gọi `RbacAuthorizationService` trước khi xử lý action cần bảo vệ.

## 5. Role Naming

Role code name không lưu prefix `ROLE_` trong database.

Đúng:

```text
ADMIN
MANAGER
STAFF
USER
```

Sai:

```text
ROLE_ADMIN
ROLE_MANAGER
```

Trong Java, `Role.authorityName()` tự convert role DB thành Spring authority:

```text
ADMIN -> ROLE_ADMIN
MANAGER -> ROLE_MANAGER
```

## 6. Where To Check RBAC

Check RBAC trong service layer, không check trong controller.

Controller pattern:

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
    return new ResponseEntity<>(userService.getUser(id), HttpStatus.OK);
  }
}
```

Service pattern:

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final RbacAuthorizationService authorizationService;

  public UserResponse getUser(Long id) {
    authorizationService.requireSelfOrPermission(id, "USER", "VIEW");
    // business logic here
  }
}
```

## 7. RbacAuthorizationService Usage

### Require A Permission

Dùng khi action chỉ dành cho user có quyền tương ứng:

```java
authorizationService.requirePermission("USER", "VIEW");
```

Check trên tương đương yêu cầu một trong hai permission:

```text
USER_VIEW
USER_MANAGE
```

### Require A Permission In An Account

Dùng cho quyền gắn với account/workspace:

```java
authorizationService.requirePermission(accountId, "RBAC", "MANAGE");
```

Check trên chỉ xét role assignment của current user trong `accountId` đó.

### Allow Self Or Permission

Dùng cho profile/user detail/update:

```java
authorizationService.requireSelfOrPermission(userId, "USER", "UPDATE");
```

Ý nghĩa:

- Nếu current user đang thao tác chính mình: cho phép.
- Nếu thao tác user khác: cần `USER_UPDATE` hoặc `USER_MANAGE`.

## 8. Account Scope Rule

Role assignment phải có account scope khi quyền liên quan tenant/workspace.

Đúng:

```java
user.replaceRolesForAccount(account, roles);
authorizationService.requirePermission(accountId, "RBAC", "MANAGE");
```

Sai:

```java
user.replaceRoles(roles); // global replacement, dễ leak quyền giữa account
authorizationService.requirePermission("RBAC", "MANAGE"); // thiếu account scope cho action tenant-specific
```

Exception: các quyền platform-level thật sự global mới được check không có `accountId`. Nếu không chắc, default là account-scoped.

## 9. Admin RBAC API Convention

Base path:

```text
/api/v1/admin/rbac
```

Current endpoints:

```text
GET    /api/v1/admin/rbac/roles
POST   /api/v1/admin/rbac/roles
PUT    /api/v1/admin/rbac/roles/{roleId}/permissions
DELETE /api/v1/admin/rbac/roles/{roleId}
GET    /api/v1/admin/rbac/permissions
PUT    /api/v1/admin/rbac/accounts/{accountId}/users/{userId}/roles
```

Controller không dùng `@PreAuthorize`. `RbacService` tự gọi:

```java
authorizationService.requirePermission("RBAC", "VIEW");
authorizationService.requirePermission("RBAC", "MANAGE");
authorizationService.requirePermission(accountId, "RBAC", "MANAGE");
```

## 10. Adding A New Protected Feature

Ví dụ thêm module `PAYMENT`.

### Step 1: Add Permission Migration

Tạo Flyway migration mới:

```sql
INSERT INTO permissions (name, code_name, module, action, description, is_active, version)
VALUES
    ('Payment View', 'PAYMENT_VIEW', 'PAYMENT', 'VIEW', 'View payments', TRUE, 0),
    ('Payment Manage', 'PAYMENT_MANAGE', 'PAYMENT', 'MANAGE', 'Manage payments', TRUE, 0)
ON CONFLICT (code_name) DO NOTHING;
```

### Step 2: Assign Permission To Role

Seed mặc định nếu cần:

```sql
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code_name IN ('PAYMENT_VIEW')
WHERE r.code_name = 'STAFF'
ON CONFLICT DO NOTHING;
```

Hoặc gán qua admin RBAC API.

### Step 3: Check In Service

```java
authorizationService.requirePermission(accountId, "PAYMENT", "VIEW");
```

## 11. Anti-Patterns

Không làm các pattern sau:

```java
@PreAuthorize("hasAuthority('USER_VIEW')")
```

```java
if (user.getRole() == UserRole.ADMIN) {
  // allow
}
```

```java
if (authentication.getAuthorities().contains("USER_VIEW")) {
  // allow
}
```

```java
authorizationService.requirePermission("ROLE", "MANAGE");
```

Thay vào đó:

```java
authorizationService.requirePermission("RBAC", "MANAGE");
```

## 12. JWT Convention

JWT không phải source of truth cho RBAC.

Access token chỉ chứa identity/session metadata:

```text
sub    = user email
userId = user id
type   = access
jti    = token id
iat    = issued at
exp    = expires at
```

Refresh token chỉ chứa identity/session metadata:

```text
sub    = user email
userId = user id
type   = refresh
jti    = token id
iat    = issued at
exp    = expires at
```

Không đưa `roles` hoặc `permissions` vào JWT để authorize.

Lý do: RBAC của ArenaX là dynamic. Admin có thể sửa role/permission runtime. Nếu token chứa permission, token cũ sẽ stale cho tới khi hết hạn.

Đúng:

```java
authorizationService.requirePermission(accountId, "RBAC", "MANAGE");
```

Sai:

```java
List<String> permissions = jwtService.extractPermissions(token);
if (permissions.contains("RBAC_MANAGE")) {
  // allow
}
```

`AuthResponse.user.roles` và `AuthResponse.user.permissions` có thể dùng để frontend render UI ngay sau login/refresh, nhưng backend vẫn phải check quyền bằng `RbacAuthorizationService`.

## 13. Quick Checklist

Khi thêm endpoint/service mới cần bảo vệ:

- Permission code có đúng format `<MODULE>_<ACTION>` không?
- Permission có bị bắt đầu bằng `ROLE_` không? Nếu có thì đổi.
- Controller có giữ mỏng và không dùng `@PreAuthorize` không?
- Service đã gọi `RbacAuthorizationService` trước business logic chưa?
- Action này có cần `accountId` không? Nếu có thì dùng overload account-scoped.
- Nếu là own-profile action, có dùng `requireSelfOrPermission` không?
- Permission đã được seed hoặc admin có thể assign được chưa?
