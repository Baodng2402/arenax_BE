# ArenaX Repository And Querydsl Convention

Tài liệu này mô tả convention viết repository và Querydsl trong ArenaX.

## 1. Repository Naming

Repository đặt theo domain/entity name, không prefix implementation technology.

Đúng:

```text
UserRepository
RoleRepository
PermissionRepository
AccountRepository
SubscriptionRepository
RefreshTokenRepository
UserRoleAssignmentRepository
```

Sai:

```text
JpaUserRepository
UserJpaRepository
UserRepositoryImpl // nếu chỉ delegate lại Spring Data
```

Lý do: service không cần biết repository đang dùng JPA, Querydsl hay implementation khác. Tên repository nên nói về domain, không nói về technology.

## 2. Repository Shape

Mỗi repository là một Spring Data interface trong package:

```text
com.bk.arenax.adapter.repository
```

Pattern chuẩn:

```java
public interface UserRepository
    extends JpaRepository<User, Long>, QuerydslPredicateExecutor<User> {
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);
}
```

Rules:

- Extend `JpaRepository<Entity, Long>` cho CRUD/pagination/sorting chuẩn.
- Extend `QuerydslPredicateExecutor<Entity>` cho dynamic/type-safe predicates.
- Thêm derived query methods khi query đơn giản và đọc được.
- Không tạo `RepositoryImpl` nếu implementation chỉ gọi lại `repository.findAll()`, `repository.save()`, `repository.findById()`.

## 3. Service Injection

Service inject repository trực tiếp theo domain name.

Đúng:

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
}
```

Sai:

```java
private final JpaUserRepository userRepository;
private final com.bk.arenax.port.repository.UserRepository userRepository;
```

Hiện tại project không dùng port-wrapper cho repository nếu wrapper chỉ delegate Spring Data.

## 4. Query Choice

Chọn query theo độ phức tạp.

### Derived Query

Dùng derived query khi điều kiện đơn giản, cố định, và tên method vẫn đọc được.

Ví dụ:

```java
Optional<User> findByEmail(String email);

boolean existsByEmail(String email);

List<Role> findByCodeNameIn(Collection<String> codeNames);
```

### Querydsl Predicate

Dùng Querydsl khi filter dynamic hoặc có nhiều điều kiện optional.

Ví dụ:

```java
QUser user = QUser.user;
BooleanExpression predicate = user.email.containsIgnoreCase(keyword)
    .and(user.status.eq(UserStatus.ACTIVE));

Iterable<User> users = userRepository.findAll(predicate);
```

### Custom Implementation

Chỉ tạo custom repository implementation khi:

- Query cần join/fetch strategy phức tạp mà `QuerydslPredicateExecutor` không đủ.
- Cần projection/custom DTO query tối ưu performance.
- Cần batch operation đặc thù.

Nếu tạo custom implementation thì phải có logic query thật, không được chỉ wrap lại Spring Data CRUD.

## 5. Pagination Convention

Với pagination đơn giản, dùng `Pageable` từ `PaginationHelper` trong service.

```java
Page<User> page = userRepository.findAll(PaginationHelper.setPage(request));
PagedResult<User> result =
    PagedResult.of(
        page.getContent(),
        request.getCurrentPage(),
        request.getPageSize(),
        page.getTotalElements());
```

Với dynamic Querydsl + pagination:

```java
QUser user = QUser.user;
BooleanExpression predicate = user.status.eq(UserStatus.ACTIVE);

Page<User> page = userRepository.findAll(predicate, PaginationHelper.setPage(request));
```

## 6. Querydsl Generated Q Types

Project đã cấu hình Querydsl trong `build.gradle.kts`:

```kotlin
implementation("com.querydsl:querydsl-jpa:$querydslVersion:jakarta")
annotationProcessor("com.querydsl:querydsl-apt:$querydslVersion:jakarta")
annotationProcessor("jakarta.persistence:jakarta.persistence-api")
```

Q classes được generate khi compile, ví dụ:

```text
QUser
QRole
QPermission
QAccount
```

Nếu IDE chưa nhận Q class, chạy:

```bash
./gradlew compileJava
```

## 7. Adding A New Repository

Ví dụ thêm `Court` entity.

Tạo file:

```text
src/main/java/com/bk/arenax/adapter/repository/CourtRepository.java
```

Nội dung:

```java
package com.bk.arenax.adapter.repository;

import com.bk.arenax.domain.court.Court;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface CourtRepository
    extends JpaRepository<Court, Long>, QuerydslPredicateExecutor<Court> {
  Optional<Court> findByCodeName(String codeName);
}
```

Service inject:

```java
private final CourtRepository courtRepository;
```

## 8. Anti-Patterns

Không viết wrapper chỉ để delegate:

```java
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
  private final JpaUserRepository jpaUserRepository;

  public Optional<User> findById(Long id) {
    return jpaUserRepository.findById(id);
  }
}
```

Không prefix technology trong tên repository:

```java
public interface JpaUserRepository extends JpaRepository<User, Long> {}
```

Không để service vừa dùng port repository vừa dùng adapter repository trong cùng module:

```java
private final UserRepository userRepository;
private final JpaUserRepository jpaUserRepository;
```

## 9. Checklist

Khi thêm/sửa repository:

- Tên repository là domain name chưa?
- Có bỏ prefix `Jpa` chưa?
- Có extend `JpaRepository<Entity, Long>` chưa?
- Có extend `QuerydslPredicateExecutor<Entity>` chưa?
- Query đơn giản đã dùng derived query chưa?
- Query dynamic đã dùng Querydsl predicate chưa?
- Có tránh tạo wrapper `RepositoryImpl` chỉ delegate chưa?
- Service import từ `com.bk.arenax.adapter.repository` chưa?
