# Running Individual Services

Tài liệu này chỉ tập trung vào một việc: chạy từng service sau khi repo đã tách sang multi-project microservices.

## 1. Kết Luận Ngắn Gọn

- Bạn vẫn dùng nút Start của IDE được.
- Bạn không còn run từ root project như monolith cũ.
- Bạn phải run đúng `*Application` class của từng service.
- Hãy dùng Spring profile `local` để khỏi phải nhập datasource args dài mỗi lần.

## 2. Profile Local Hoạt Động Thế Nào

Mỗi service hiện có `application-local.yaml` riêng.

Profile `local` đã cấu hình sẵn:

- `server.port`
- datasource cho các persistence service

Default hiện tại:

```text
host     localhost
port     5432
user     postgres
password postgres
```

Default database name theo service:

- identity -> `arenax_identity`
- access -> `arenax_access`
- tenant -> `arenax_tenant`
- subscription -> `arenax_subscription`
- competition -> `arenax_competition`
- ranking -> `arenax_ranking`

Nếu cần đổi, chỉ cần set env vars như:

```bash
ARENAX_DB_HOST=localhost
ARENAX_DB_PORT=5432
ARENAX_DB_USER=postgres
ARENAX_DB_PASSWORD=postgres
ARENAX_IDENTITY_DB=arenax_identity
```

## 3. Chạy Service Bằng CLI

### Cách ngắn nhất

```bash
bin/run-service gateway
bin/run-service identity
bin/run-service competition
```

Script này tự thêm:

```text
--spring.profiles.active=local
```

### Cách Gradle trực tiếp

```bash
./gradlew :services:api-gateway:bootRun --args='--spring.profiles.active=local'
./gradlew :services:identity-service:bootRun --args='--spring.profiles.active=local'
./gradlew :services:competition-service:bootRun --args='--spring.profiles.active=local'
```

## 4. Chạy Service Bằng IDE

Bạn vẫn chạy bằng IDE được, nhưng nên hiểu phần này theo mức overview:

- root project không còn runnable như monolith cũ
- phải chạy đúng `*Application` class của từng service
- luôn dùng profile `local`
- có thể chạy nhiều service cùng lúc bằng nhiều Run Configuration hoặc Compound configuration

Main class theo service:

- `api-gateway` -> `com.bk.arenax.gateway.ApiGatewayApplication`
- `identity-service` -> `com.bk.arenax.identity.IdentityServiceApplication`
- `access-service` -> `com.bk.arenax.access.AccessServiceApplication`
- `tenant-service` -> `com.bk.arenax.tenant.TenantServiceApplication`
- `subscription-service` -> `com.bk.arenax.subscription.SubscriptionServiceApplication`
- `competition-service` -> `com.bk.arenax.competition.CompetitionServiceApplication`
- `ranking-service` -> `com.bk.arenax.ranking.RankingServiceApplication`

Nếu bạn cần hướng dẫn IntelliJ chi tiết theo kiểu click-by-click, đọc file riêng:

- `docs/development/intellij-setup.md`

## 5. Port Map

Khi chạy với profile `local`, port mặc định là:

- gateway -> `8080`
- identity -> `8081`
- access -> `8082`
- tenant -> `8083`
- subscription -> `8084`
- competition -> `8085`
- ranking -> `8086`

## 6. Step-By-Step Setup Cho Người Mới

### Bước 1: Dựng PostgreSQL

Nếu chưa có PostgreSQL local, chạy nhanh bằng Docker:

```bash
docker run --name arenax-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16
```

### Bước 2: Tạo Database

```bash
docker exec -it arenax-postgres psql -U postgres -c "CREATE DATABASE arenax_identity;"
docker exec -it arenax-postgres psql -U postgres -c "CREATE DATABASE arenax_access;"
docker exec -it arenax-postgres psql -U postgres -c "CREATE DATABASE arenax_tenant;"
docker exec -it arenax-postgres psql -U postgres -c "CREATE DATABASE arenax_subscription;"
docker exec -it arenax-postgres psql -U postgres -c "CREATE DATABASE arenax_competition;"
docker exec -it arenax-postgres psql -U postgres -c "CREATE DATABASE arenax_ranking;"
```

### Bước 3: Chạy Test Trước

```bash
./gradlew test
```

### Bước 4: Start Gateway

```bash
bin/run-service gateway
```

### Bước 5: Start Service Bạn Đang Làm

Ví dụ:

```bash
bin/run-service identity
bin/run-service competition
```

Hoặc bấm Start trong IDE với profile `local`.

Nếu bạn muốn chạy 2-3 service cùng lúc bằng IntelliJ, dùng nhiều Run Configuration hoặc Compound configuration theo hướng dẫn tại:

- `docs/development/intellij-setup.md`

## 7. Khi Nào Dùng Script, Khi Nào Dùng IDE

### Dùng IDE khi:

- bạn đang debug một service cụ thể
- bạn muốn đặt breakpoint
- bạn đang sửa controller/service/repository của một module
- bạn muốn chạy 2-3 service song song bằng nhiều Run Configuration hoặc Compound configuration

### Dùng CLI script khi:

- bạn muốn bật nhanh service để smoke test
- bạn muốn copy-paste command ngắn cho người khác
- bạn muốn start nhiều service bằng background script

## 8. Start Nhiều Service Cùng Lúc

```bash
bin/run-local-stack start
bin/run-local-stack status
bin/run-local-stack stop
```

Logs nằm ở:

```text
.local/run/
```

## 9. Lỗi Thường Gặp

### `Task 'bootRun' not found in root project`

Bạn đang run ở root như monolith cũ. Hãy run subproject cụ thể hoặc dùng `bin/run-service`.

### `Failed to configure a DataSource`

Thường là bạn chưa dùng profile `local`, hoặc local DB chưa tồn tại, hoặc env override bị sai.

### `Connection refused` từ gateway

Thường là service đích chưa chạy hoặc không chạy đúng port local mặc định.
