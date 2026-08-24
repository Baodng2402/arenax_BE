> **Quickstart canonical** — nếu bạn chỉ muốn biết cách chạy repo local để bắt đầu code, đọc file này trước. Chi tiết hơn nằm ở [Development Guide](./README.md).

# Local Development

## Prerequisites

- JDK 21
- Docker

## Quick Reality Check

Repo này là multi-service Gradle monorepo, không phải app runnable ở root.

Điều đó có nghĩa là:

- dùng root cho `./gradlew test`
- không chạy app từ root như monolith cũ
- chạy từng service bằng task dạng `:services:<service>:bootRun`

Ví dụ:

```bash
./gradlew :services:api-gateway:bootRun
./gradlew :services:identity-service:bootRun
```

Persistence services nên chạy với profile `local`.

## Quick Start

1. Start infra:

   ```bash
   docker compose up -d
   ```

2. Run the full test suite once:

   ```bash
   ./gradlew test
   ```

3. Start the service you need:

   ```bash
   ./gradlew :services:identity-service:bootRun --args='--spring.profiles.active=local'
   ./gradlew :services:api-gateway:bootRun --args='--spring.profiles.active=local'
   ```

4. Check the main entrypoints:

   - gateway health: `http://localhost:8080/actuator/health`
   - Eureka dashboard: `http://localhost:8761`

## Useful Commands

```bash
./gradlew projects
./gradlew test
./gradlew :services:identity-service:test
./gradlew :services:competition-service:test
./gradlew :services:api-gateway:bootRun
./gradlew :services:identity-service:bootRun --args='--spring.profiles.active=local'
docker compose up -d
docker compose ps
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

Đọc tiếp theo nhu cầu:

- `docs/development/README.md`
- `docs/development/running-the-stack.md`
- `docs/development/running-services.md`
- `docs/development/intellij-setup.md`
