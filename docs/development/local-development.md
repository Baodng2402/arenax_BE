# Local Development

## Prerequisites

- JDK 21
- PostgreSQL 16 available locally or through Docker
- Docker available if you want the quick local PostgreSQL setup below

## Current Scope

The repository is currently source-first:

- service code and tests are implemented
- RabbitMQ broker runtime is wired (outbox relay + listeners; broker provided by `compose.yaml`)
- Docker Compose defines Postgres, Redis và discovery-server (`compose.yaml`)
- CI/CD và VPS deployment chưa được định nghĩa

## Important Reality Check

Sau khi restructure, root project không còn là một Spring Boot app runnable.

Điều đó có nghĩa là:

- bạn vẫn dùng root cho `./gradlew test`
- nhưng bạn không chạy app từ root như monolith cũ nữa
- bạn phải chạy từng subproject bằng task dạng `:services:<service>:bootRun`

Để giảm friction, repo hiện có thêm:

- profile `local` cho từng service
- `bin/run-service`
- `bin/run-local-stack`

Theo Gradle multi-project syntax, task của subproject được gọi bằng full project path, ví dụ:

```bash
./gradlew :services:api-gateway:bootRun
./gradlew :services:identity-service:bootRun
```

Theo Spring Boot Gradle plugin, `bootRun` là task dùng để chạy application của subproject khi plugin Spring Boot được apply.

## Useful Commands

```bash
./gradlew projects
./gradlew test
./gradlew :services:identity-service:test
./gradlew :services:competition-service:test
./gradlew :services:api-gateway:bootRun
bin/run-service gateway
bin/run-service identity
bin/run-local-stack start
```

## Current Startup Limitation

`api-gateway` có thể start ngay.

Các persistence service như:

- `identity-service`
- `tenant-service`
- `subscription-service`
- `competition-service`
- `ranking-service`

hiện sẽ fail nếu bạn chỉ chạy `bootRun` trần mà không bật profile `local`.

Lỗi điển hình hiện tại:

```text
Failed to configure a DataSource: 'url' attribute is not specified
```

Điều này là expected với trạng thái source-first hiện tại, không phải do Gradle task bị sai.

## Running Individual Services

Each service has:

- `application.yaml` for shared defaults
- `application-local.yaml` for local runtime settings

Gateway currently expects downstream services at these ports:

- `identity-service`: `8081`
- `tenant-service`: `8083`
- `subscription-service`: `8084`
- `competition-service`: `8085`
- `ranking-service`: `8086`

Gateway route defaults live in `services/api-gateway/src/main/resources/application.yaml`.

Quan trọng: port local hiện được set trong `application-local.yaml` của từng service. Vì vậy khi chạy với profile `local`, bạn không cần tự truyền `--server.port=...` nữa.

Đọc thêm:

- `docs/development/running-the-stack.md`
- `docs/development/running-services.md`
- `docs/development/intellij-setup.md`
