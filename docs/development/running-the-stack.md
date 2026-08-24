> **Reference** — file này giải thích local runtime stack chi tiết hơn. Nếu bạn chỉ cần quickstart, bắt đầu từ [local-development.md](./local-development.md).

# Running The Stack Locally

File này tập trung vào local runtime stack: Docker services nào cần có, chúng dùng để làm gì, và kiểm tra health ra sao.

## 1. Runtime Components

`compose.yaml` hiện định nghĩa các thành phần local sau:

- `postgres` on `5432`
- `redis` on `6379`
- `discovery-server` on `8761`
- `rabbitmq` on `5672` and `15672`

Business services (`identity-service`, `tenant-service`, `subscription-service`, `competition-service`, `ranking-service`) được chạy riêng bằng Gradle `bootRun`, không nằm trong `compose.yaml`.

## 2. Start Infra

Trước khi chạy persistence services hoặc event-driven flows, start local infra:

```bash
docker compose up -d
docker compose ps
```

Postgres được khởi tạo với:

- user: `postgres`
- password: `12345`

Các database service-level được tạo từ `docker/postgres/init-databases.sql`.

## 3. Optional Verification

Nếu bạn muốn verify code trước khi start runtime:

```bash
./gradlew test
```

## 4. Smoke Checks

- **Eureka Dashboard:** [http://localhost:8761](http://localhost:8761) (Xem các service đăng ký).
- **RabbitMQ Management:** [http://localhost:15672](http://localhost:15672)
- **API Gateway Health:**
  ```bash
  curl http://localhost:8080/actuator/health
  ```

## 5. Troubleshooting

- **Lỗi `Task 'bootRun' not found in root project`:**
  Root project không phải là Spring Boot app. Hãy chạy chỉ định đúng module, ví dụ: `./gradlew :services:api-gateway:bootRun`.
- **Lỗi kết nối database:**
  Đảm bảo `docker compose up -d` đang chạy, password local đúng với `compose.yaml`, và database đã được khởi tạo qua init script.
- **Lỗi Eureka Connection Refused:**
  Đảm bảo `discovery-server` đã healthy (port `8761`) trước khi boot các service con.

## 6. Read Next

- `running-services.md` nếu bạn muốn run từng service
- `intellij-setup.md` nếu bạn muốn làm việc qua IntelliJ
- `testing.md` nếu bạn muốn verify nhanh trước khi push
