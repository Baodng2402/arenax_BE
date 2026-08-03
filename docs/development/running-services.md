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

> ⚠️ Nếu dùng `compose.yaml` ở root (`docker compose up -d`) thì password là **`12345`**, khác default `postgres`. Lúc đó phải export trước khi chạy service:
>
> ```bash
> export ARENAX_DB_PASSWORD=12345
> ```
>
> `compose.yaml` mount `docker/postgres/init-databases.sql` nên các DB dưới sẽ được tạo tự động.

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

### Bước 3.5: Tạo Local JWT Key Cho Identity

`identity-service` không tự sinh runtime signing key.

Test thì vẫn chạy được ngay vì test dùng key fixture riêng trong `src/test/resources`, nhưng nếu bạn muốn chạy `identity-service` hoặc `api-gateway` local thật thì cần tự chuẩn bị 1 cặp RSA PEM.

Cách nhanh nhất — script sinh key sẵn (đúng format PKCS#8, không ghi đè key cũ):

```bash
bin/generate-jwt-keys.sh
```

Script này tạo:

```text
secrets/identity-private.pem  (PKCS#8 — "BEGIN PRIVATE KEY")
secrets/identity-public.pem   (X.509 — "BEGIN PUBLIC KEY")
```

Nếu muốn tự chạy `openssl` thì phải dùng đúng format sau:

```bash
mkdir -p secrets
openssl genpkey -algorithm RSA -out secrets/identity-private.pem -pkeyopt rsa_keygen_bits:2048
openssl pkey -in secrets/identity-private.pem -pubout -out secrets/identity-public.pem
```

> ⚠️ **Không dùng `openssl genrsa`** — nó tạo PKCS#1 (`BEGIN RSA PRIVATE KEY`), trong khi `identity-service` đọc key bằng `PKCS8EncodedKeySpec` nên service sẽ fail khi start với lỗi `InvalidKeySpecException`.

Mặc định `identity-service` sẽ đọc đúng 2 file này:

```text
./secrets/identity-private.pem
./secrets/identity-public.pem
```

Nếu muốn dùng path khác, set env vars:

```bash
export ARENAX_JWT_KEY_ID=arenax-identity-key-1
export ARENAX_JWT_PRIVATE_KEY_LOCATION=file:/absolute/path/to/identity-private.pem
export ARENAX_JWT_PUBLIC_KEY_LOCATION=file:/absolute/path/to/identity-public.pem
```

`secrets/` đã nằm trong `.gitignore` — không bao giờ commit private key lên git.

Gateway không cần private key. Gateway chỉ cần đọc public JWKS từ Identity. Local default hiện tại là:

```text
http://localhost:8081/.well-known/jwks.json
```

Nghĩa là local flow chuẩn sẽ là:

- start `identity-service`
- start `api-gateway`
- gateway tự verify access token qua JWKS của identity

### Bước 4: Start Gateway

> ⚠️ **Quan trọng — Eureka:** route mặc định của gateway là `lb://identity-service` (load balancer qua discovery). Repo này **không có Eureka server**, nên nếu chạy gateway local mà không override route, mọi request sẽ lỗi `No servers available for identity-service`.

Không có Eureka, override route sang URL trực tiếp khi start:

```bash
bin/run-service gateway --arenax.gateway.routes.identity-service=http://localhost:8081
```

Hoặc Gradle trực tiếp:

```bash
./gradlew :services:api-gateway:bootRun \
  --args='--spring.profiles.active=local --arenax.gateway.routes.identity-service=http://localhost:8081'
```

(Khi nào repo có Eureka server chạy local thì bỏ override này đi.)

### Bước 5: Start Service Bạn Đang Làm

Ví dụ:

```bash
bin/run-service identity
bin/run-service competition
```

Hoặc bấm Start trong IDE với profile `local`.

Nếu bạn muốn chạy 2-3 service cùng lúc bằng IntelliJ, dùng nhiều Run Configuration hoặc Compound configuration theo hướng dẫn tại:

- `docs/development/intellij-setup.md`

### Bước 6: Smoke Test Luồng Identity

Sau khi `identity-service` (8081) và `api-gateway` (8080) đã chạy, test luồng end-to-end qua gateway:

```bash
# 1. Đăng ký user mới (trả về 201, user ở trạng thái PENDING)
curl -X POST localhost:8080/api/v1/auth/register -H 'Content-Type: application/json' \
  -d '{"email":"dev@arenax.dev","password":"Sup3rSecret!","fullName":"Dev User"}'

# 2. Lấy verification token từ bảng outbox (chưa có email thật — RabbitMQ chưa wiring)
psql -U postgres -d arenax_identity -c \
  "select payload from outbox_events where event_type='identity.user.verification-requested.v1' order by created_at desc limit 1;"
# → payload chứa "verificationToken": "...", lấy giá trị đó cho bước 3

# 3. Verify email (user chuyển sang ACTIVE)
curl -X POST localhost:8080/api/v1/auth/verify-email -H 'Content-Type: application/json' \
  -d '{"token":"<verificationToken>"}'

# 4. Login — lưu cookie refresh vào file, accessToken nằm trong JSON response
curl -c /tmp/cookies.txt -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"dev@arenax.dev","password":"Sup3rSecret!"}'

# 5. Lấy profile qua gateway (JWT được gateway verify + chèn header X-Arenax-*)
curl localhost:8080/api/v1/users/me -H "Authorization: Bearer <accessToken>"

# 6. Refresh token rotation (cookie cũ bị revoke, trả cookie mới)
curl -b /tmp/cookies.txt -X POST localhost:8080/api/v1/auth/refresh
```

Chú ý: user **PENDING** vẫn login được (200, `status=PENDING` trong response) — app nên hiện thông báo verify. User **SUSPENDED/DEACTIVATED** sẽ bị chặn với 403.

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
