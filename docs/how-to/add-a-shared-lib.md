# How To Add A Shared Lib

File này dùng khi bạn thực sự cần thêm code vào `libs/` hoặc tạo một shared lib mới.

## Khi Nào Được Dùng `libs/`

Chỉ đưa code vào `libs/` nếu thỏa các điều kiện sau:

- đó là shared technical implementation ổn định
- nhiều service cần import trực tiếp để chạy
- code đó không mang business ownership của riêng một service

Ví dụ phù hợp:

- event envelope dùng chung
- outbox relay contract dùng chung
- security/filter kỹ thuật ổn định

Ví dụ không phù hợp:

- JPA entity
- repository
- business service
- request/response DTO giữa services
- event payload Java classes dùng chung
- migration
- business enum làm các service bị release cùng nhau

Nếu còn phân vân giữa `duplicate nhỏ` và `shared lib`, ưu tiên duplicate nhỏ để giữ boundary rõ.

## Dùng Shared Lib Có Cần Publish Jar Riêng Không?

Không. Trong repo này, `libs/*` là Gradle subprojects nằm trong cùng multi-project build.

Flow sử dụng là:

1. tạo code trong `libs/<lib-name>/src/...`
2. nếu là lib mới, thêm module vào `settings.gradle.kts`
3. thêm dependency `implementation(project(":libs:<lib-name>"))` ở service cần dùng
4. chạy `./gradlew test` hoặc task của service; Gradle sẽ tự build dependency chain

Bạn không cần publish artifact riêng chỉ để dùng trong cùng repo.

## Cách Tạo Một Lib Mới

### 1. Tạo module

Tạo thư mục theo pattern:

```text
libs/<lib-name>/
├── build.gradle.kts
└── src/main/java/...
```

`build.gradle.kts` thường bắt đầu từ `java-library` và chỉ thêm dependency kỹ thuật thật sự cần.

### 2. Include vào Gradle settings

Thêm vào `settings.gradle.kts`:

```kotlin
include(":libs:<lib-name>")
project(":libs:<lib-name>").projectDir = file("libs/<lib-name>")
```

### 3. Dùng ở service cần import

Ví dụ trong `services/identity-service/build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":libs:<lib-name>"))
}
```

### 4. Giữ API của lib nhỏ và rõ

- expose ít entry point thôi
- tránh để service consumers phải đọc internals mới hiểu cách dùng
- không để module thành "common junk drawer"

## Review Checklist

Trước khi merge một shared lib mới, tự check:

- code này có thật sự là technical shared concern không?
- nếu bỏ vào `libs/`, boundary service nào sẽ bị mờ?
- có đang lén share business model giữa services không?
- đã có ít nhất một consumer thật sự chưa?
- docs cần update ở đâu: `docs/architecture/conventions.md`, `docs/services/<service>.md`, hay `docs/overview.md`?

## Related Docs

- `../architecture/conventions.md`
- `../architecture/service-boundaries.md`
- `../development/local-development.md`
