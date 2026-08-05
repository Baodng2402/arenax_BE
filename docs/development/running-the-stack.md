# Running The Stack Locally

Tài liệu này hướng dẫn chi tiết cách chạy hệ thống microservices Arenax-BE trên môi trường local, tận dụng **Docker Compose**, **Spring Boot Docker Compose Integration**, và cấu trúc multi-module mới.

---

## 1. Tổng Quan Kiến Trúc Local Runtime

Hệ thống hiện tại bao gồm:
- **`discovery-server`** (Eureka Server): Cổng Service Discovery (port `8761`).
- **`api-gateway`**: API Gateway định tuyến request (port `8080`).
- **`identity-service`**: Quản lý Authentication (JWT) và RBAC (đã hợp nhất access-service).
- **`tenant-service`**, **`subscription-service`**, **`competition-service`**, **`ranking-service`**: Các service nghiệp vụ.
- **PostgreSQL & Redis**: Infrastructure cơ sở dữ liệu và cache.

Nhờ cấu hình **`spring-boot-docker-compose`** ở root, khi bạn chạy bất kỳ service nào (`bootRun`), Spring Boot sẽ **tự động kiểm tra và khởi chạy các container (Postgres, Redis, Eureka) từ `compose.yaml`** ở root project nếu chúng chưa chạy. Nếu đã chạy, Spring Boot sẽ tự động kết nối mà không làm gián đoạn.

---

## 2. Bước 1: Kiểm Tra Build & Tests

Trước khi chạy runtime, hãy đảm bảo toàn bộ project compile và test thành công:

```bash
./gradlew test
```

---

## 3. Bước 2: Khởi Chạy Hạ Tầng (Docker Compose)

Bạn có thể để Spring Boot tự động bật hạ tầng khi chạy service, hoặc chủ động khởi chạy trước toàn bộ hạ tầng bằng Docker Compose:

```bash
docker compose up -d
```

Lệnh này sẽ khởi động:
- **Postgres** (port `5432`, tự động tạo các database cần thiết qua init script).
- **Redis** (port `6379`).
- **Discovery Server (Eureka)** (port `8761`).

Kiểm tra trạng thái container:
```bash
docker compose ps
```

---

## 4. Bước 3: Chạy Các Service

Bạn có thể chạy service thông qua Gradle CLI với profile `local`:

### 4.1. Chạy Identity Service (Authentication & RBAC)
```bash
./gradlew :services:identity-service:bootRun --args='--spring.profiles.active=local'
```

### 4.2. Chạy API Gateway
```bash
./gradlew :services:api-gateway:bootRun --args='--spring.profiles.active=local'
```

### 4.3. Chạy Các Service Nghiệp Vụ Khác
- **Tenant Service:**
  ```bash
  ./gradlew :services:tenant-service:bootRun --args='--spring.profiles.active=local'
  ```
- **Subscription Service:**
  ```bash
  ./gradlew :services:subscription-service:bootRun --args='--spring.profiles.active=local'
  ```
- **Competition Service:**
  ```bash
  ./gradlew :services:competition-service:bootRun --args='--spring.profiles.active=local'
  ```
- **Ranking Service:**
  ```bash
  ./gradlew :services:ranking-service:bootRun --args='--spring.profiles.active=local'
  ```

---

## 5. Chạy Bằng IDE (IntelliJ IDEA)

1. Mở project trong IntelliJ IDEA.
2. Đảm bảo Gradle đã sync thành công.
3. Tìm đến class `*Application` của service muốn chạy (ví dụ: `com.bk.arenax.identity.IdentityServiceApplication`).
4. Tạo Run Configuration:
   - **Main class:** `com.bk.arenax.identity.IdentityServiceApplication`
   - **Active profiles:** `local`
   - **Environment variables:** (nếu cần override DB/Redis)
5. Nhấn **Run**. Spring Boot sẽ tự động kết nối với các container Docker đang chạy.

---

## 6. Kiểm Tra Sau Khi Khởi Chạy

- **Eureka Dashboard:** [http://localhost:8761](http://localhost:8761) (Xem các service đăng ký).
- **API Gateway Health:**
  ```bash
  curl http://localhost:8080/actuator/health
  ```
- **Identity Service Register Flow:**
  ```bash
  curl -X POST http://localhost:8080/api/v1/auth/register \
    -H 'Content-Type: application/json' \
    -d '{
      "email":"user1@example.com",
      "password":"secret123",
      "displayName":"User One"
    }'
  ```

---

## 7. Troubleshooting Nhanh

- **Lỗi `Task 'bootRun' not found in root project`:**
  Root project không phải là Spring Boot app. Hãy chạy chỉ định đúng module, ví dụ: `./gradlew :services:api-gateway:bootRun`.
- **Lỗi kết nối database:**
  Đảm bảo `docker compose up -d` đang chạy và các database đã được khởi tạo qua init script.
- **Lỗi Eureka Connection Refused:**
  Đảm bảo `discovery-server` đã healthy (port `8761`) trước khi boot các service con.
