# Running The Stack Locally

Tài liệu này hướng dẫn chạy project sau khi restructure sang multi-project microservices.

Nó viết theo trạng thái hiện tại của repo, không giả định đã có Docker Compose hoặc RabbitMQ runtime wiring.

## 1. Hiểu Đúng Trước Khi Chạy

Hiện tại có 3 sự thật quan trọng:

1. Root project không còn là app runnable.
2. `api-gateway` start được ngay.
3. Các persistence service nên chạy với Spring profile `local`.

Nếu bạn thử kiểu monolith cũ và thấy app không lên, đó là vì kiến trúc đã đổi.

Nếu bạn cần hướng dẫn chi tiết riêng cho IntelliJ và chạy 2-3 service cùng lúc trong IDE, xem thêm:

- `docs/development/intellij-setup.md`

## 2. Bước 1: Kiểm Tra Build Trước

Từ root repo:

```bash
./gradlew test
```

Nếu bước này chưa xanh thì chưa nên chạy service runtime.

## 3. Bước 2: Xem Các Module Hiện Có

```bash
./gradlew projects
```

Bạn sẽ thấy các module dưới `services:`.

Các app runnable hiện tại nằm ở:

- `:services:api-gateway`
- `:services:identity-service`
- `:services:access-service`
- `:services:tenant-service`
- `:services:subscription-service`
- `:services:competition-service`
- `:services:ranking-service`

## 4. Bước 3: Start Gateway Trước Để Smoke Check

Đây là app dễ start nhất vì không phụ thuộc datasource.

```bash
./gradlew :services:api-gateway:bootRun
```

Expected result:

- app start thành công
- Tomcat lên ở port `8080`
- `/actuator/health` có thể truy cập được

Ví dụ check nhanh:

```bash
curl http://localhost:8080/actuator/health
```

## 5. Vì Sao Các Service Persistence Không Lên Khi Chạy Trần

Ví dụ command này:

```bash
./gradlew :services:identity-service:bootRun
```

hiện sẽ fail nếu chưa truyền datasource config hoặc chưa bật profile `local`.

Lỗi expected:

```text
Failed to configure a DataSource: 'url' attribute is not specified
```

Lý do là:

- service có Spring Data JPA + Flyway
- nhưng `application.yaml` mặc định chưa có local datasource config
- runtime classpath main cũng không có embedded H2 như test classpath

Vì vậy local runtime nên dùng:

```bash
--spring.profiles.active=local
```

## 6. Bước 4: Chuẩn Bị Một PostgreSQL Local

Bạn có thể dùng PostgreSQL cài sẵn hoặc chạy nhanh bằng Docker.

### Option A: PostgreSQL local đã cài sẵn

Chỉ cần đảm bảo bạn có một PostgreSQL server đang chạy trên `localhost:5432`.

### Option B: PostgreSQL bằng Docker

```bash
docker run --name arenax-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16
```

Chờ vài giây rồi tạo các database:

```bash
docker exec -it arenax-postgres psql -U postgres -c "CREATE DATABASE arenax_identity;"
docker exec -it arenax-postgres psql -U postgres -c "CREATE DATABASE arenax_access;"
docker exec -it arenax-postgres psql -U postgres -c "CREATE DATABASE arenax_tenant;"
docker exec -it arenax-postgres psql -U postgres -c "CREATE DATABASE arenax_subscription;"
docker exec -it arenax-postgres psql -U postgres -c "CREATE DATABASE arenax_competition;"
docker exec -it arenax-postgres psql -U postgres -c "CREATE DATABASE arenax_ranking;"
```

## 7. Bước 5: Start Từng Service Với Profile `local`

Nếu không muốn nhớ command dài, repo giờ có sẵn helper script:

```bash
bin/run-service gateway
bin/run-service identity
bin/run-service competition
```

Script này tự:

- tên service -> Gradle subproject
- bật profile `local`
- dùng port và datasource từ `application-local.yaml`

Default database config của profile `local`:

```text
host     localhost
port     5432
user     postgres
password postgres
```

Bạn có thể override bằng environment variables như:

```bash
ARENAX_DB_HOST=localhost \
ARENAX_DB_PORT=5432 \
ARENAX_DB_USER=postgres \
ARENAX_DB_PASSWORD=postgres \
bin/run-service identity
```

Hoặc override database name riêng cho từng service:

```bash
ARENAX_IDENTITY_DB=my_identity_db bin/run-service identity
```

### 7.1 Identity Service

```bash
./gradlew :services:identity-service:bootRun --args='--spring.profiles.active=local'
```

Equivalent helper command:

```bash
bin/run-service identity
```

### 7.2 Access Service

```bash
./gradlew :services:access-service:bootRun --args='--spring.profiles.active=local'
```

Equivalent helper command:

```bash
bin/run-service access
```

### 7.3 Tenant Service

```bash
./gradlew :services:tenant-service:bootRun --args='--spring.profiles.active=local'
```

Equivalent helper command:

```bash
bin/run-service tenant
```

### 7.4 Subscription Service

```bash
./gradlew :services:subscription-service:bootRun --args='--spring.profiles.active=local'
```

Equivalent helper command:

```bash
bin/run-service subscription
```

### 7.5 Competition Service

```bash
./gradlew :services:competition-service:bootRun --args='--spring.profiles.active=local'
```

Equivalent helper command:

```bash
bin/run-service competition
```

### 7.6 Ranking Service

```bash
./gradlew :services:ranking-service:bootRun --args='--spring.profiles.active=local'
```

Equivalent helper command:

```bash
bin/run-service ranking
```

### 7.7 API Gateway

Sau khi các downstream service đã lên đúng port:

```bash
./gradlew :services:api-gateway:bootRun --args='--spring.profiles.active=local'
```

Equivalent helper command:

```bash
bin/run-service gateway
```

Gateway sẽ chạy ở `8080` và route tới `8081` -> `8086` theo config hiện tại.

## 8. Chạy Cả Stack Bằng Một Script

Nếu bạn muốn start toàn bộ local stack ở background:

```bash
bin/run-local-stack start
```

Check trạng thái:

```bash
bin/run-local-stack status
```

Stop tất cả process đã được script track:

```bash
bin/run-local-stack stop
```

Log sẽ được ghi vào:

```text
.local/run/
```

Lưu ý quan trọng:

- script này không tạo PostgreSQL cho bạn
- script này không tạo database giúp bạn
- script này không hoàn thiện RabbitMQ runtime
- script chỉ giúp chạy local theo config và port mặc định nhanh hơn

## 9. Dùng Nút Start Của IDE Có Được Không?

Có.

Bạn không bắt buộc phải run bằng CLI.

Điểm thay đổi sau khi restructure là:

- bạn không run root project nữa
- bạn run đúng `*Application` class của từng service
- với service trong IDE, chỉ cần chọn đúng main class và set active profile `local`

Ví dụ với IntelliJ hoặc IDE tương tự:

### Gateway

- Main class: `com.bk.arenax.gateway.ApiGatewayApplication`
- Active profile: `local`
- Chạy được ngay

### Identity

- Main class: `com.bk.arenax.identity.IdentityServiceApplication`
- Active profile: `local`

### Competition

- Main class: `com.bk.arenax.competition.CompetitionServiceApplication`
- Active profile: `local`

Tương tự cho các persistence service khác.

Nói ngắn gọn:

- IDE Start vẫn dùng được
- chỉ là bạn phải start đúng subproject app, không phải root
- gateway bấm Start là lên ngay với profile `local`
- persistence service bấm Start được sau khi set profile `local`

Nếu muốn hướng dẫn click-by-click cho IntelliJ, Run Configuration, và Compound configuration, đọc file riêng:

- `docs/development/intellij-setup.md`

## 10. Bước 6: Check Từng App Sau Khi Lên

Ví dụ check health gateway:

```bash
curl http://localhost:8080/actuator/health
```

Ví dụ flow register qua gateway:

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "email":"user1@example.com",
    "password":"secret123",
    "displayName":"User One"
  }'
```

Lưu ý:

- register chỉ mới tạo `PROVISIONING` user
- onboarding event hiện mới hoàn thiện ở source/service-layer pattern
- chưa có RabbitMQ runtime wiring để full distributed onboarding chạy tự động ngoài test flow

Nghĩa là local runtime hiện phù hợp nhất cho:

- start gateway
- start từng service riêng lẻ
- smoke test HTTP slice độc lập

chứ chưa phải full async platform end-to-end production-like.

## 11. Cách Chạy Nhanh Nhất Nếu Bạn Chỉ Muốn Verify Repo Sống

Nếu chưa muốn dựng đủ PostgreSQL cho 6 service, hãy làm theo mức tối thiểu này:

1. chạy `./gradlew test`
2. chạy `./gradlew :services:api-gateway:bootRun`
3. đọc `docs/onboarding/02-core-flows.md`
4. chọn một service rồi chạy riêng nó với profile `local`

Đây là cách ít friction nhất để bắt đầu với trạng thái hiện tại.

## 12. Những Gì Chưa Được Tự Động Hóa

Hiện repo vẫn chưa có:

- Docker Compose mới cho full stack
- RabbitMQ runtime để tự đẩy event giữa services

Script + local profile đã giúp local run nhẹ hơn, nhưng repo vẫn chưa thành one-click full platform hoàn chỉnh.

## 13. Troubleshooting Nhanh

### Lỗi: `Task 'bootRun' not found in root project`

Bạn đang chạy sai chỗ.

Đúng:

```bash
./gradlew :services:api-gateway:bootRun
```

Sai:

```bash
./gradlew bootRun
```

vì root không còn là Spring Boot app.

### Lỗi: `Failed to configure a DataSource`

Thường là bạn quên bật profile `local`, hoặc local PostgreSQL/database chưa tồn tại.

### Lỗi: gateway lên nhưng route sang service khác bị connection refused

Nguyên nhân thường là:

- downstream service chưa chạy
- service chạy sai port
- gateway đang route tới `8081` -> `8086` nhưng service vẫn đang ở `8080`

### Lỗi: register/login không chạy end-to-end như kỳ vọng microservices thật

Hiện async runtime wiring chưa hoàn thiện. Repo mới ở phase source architecture + tested slices, chưa phải full distributed runtime.
