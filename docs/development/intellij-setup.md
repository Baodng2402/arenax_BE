# IntelliJ Setup

Tài liệu này dành cho người mới vào repo và muốn chạy service trực tiếp bằng IntelliJ IDEA.

Mục tiêu là sau khi đọc xong, bạn sẽ:

- import project đúng cách
- run được 1 service bằng nút Start
- run được 2-3 service cùng lúc trong IDE
- biết lúc nào nên dùng Run riêng và lúc nào nên dùng Compound configuration

## 1. Hiểu Đúng Trạng Thái Repo

Repo này không còn là một monolith runnable ở root nữa.

Điều đó có nghĩa là:

- bạn vẫn mở toàn bộ repo ở root `arenax-be`
- bạn vẫn chạy `./gradlew test` từ root khi cần verify
- nhưng khi chạy app trong IDE, bạn phải chạy từng service riêng

Các service hiện có:

- `api-gateway`
- `identity-service`
- `access-service`
- `tenant-service`
- `subscription-service`
- `competition-service`
- `ranking-service`

## 2. Import Project Vào IntelliJ

1. Mở IntelliJ IDEA.
2. Chọn `Open`.
3. Chọn folder root của repo: `arenax-be`.
4. Chờ IntelliJ import Gradle project xong.

Nếu IDE hỏi:

- trust project
- load Gradle project
- enable auto-import

thì chọn đồng ý.

## 3. Kiểm Tra Gradle Sync

Sau khi import, bạn nên kiểm tra:

1. Mở tab `Gradle`.
2. Xác nhận IDE thấy các project con dưới `services/`.
3. Nếu chưa thấy, bấm `Reload All Gradle Projects`.

Bạn nên thấy các module tương ứng với từng service.

## 4. Profile Local

Khi chạy service trong IDE, hãy dùng Spring profile `local`.

Profile `local` hiện đã cấu hình sẵn:

- `server.port`
- datasource cho các persistence service

Port mặc định:

- gateway -> `8080`
- identity -> `8081`
- access -> `8082`
- tenant -> `8083`
- subscription -> `8084`
- competition -> `8085`
- ranking -> `8086`

Database mặc định cho local profile:

- identity -> `arenax_identity`
- access -> `arenax_access`
- tenant -> `arenax_tenant`
- subscription -> `arenax_subscription`
- competition -> `arenax_competition`
- ranking -> `arenax_ranking`

## 5. Chạy Thử Service Dễ Nhất

Service dễ start nhất là `api-gateway` vì không cần PostgreSQL.

Các bước:

1. Mở file `services/api-gateway/src/main/java/com/bk/arenax/gateway/ApiGatewayApplication.java`.
2. Bấm nút Run ở cạnh hàm `main`.
3. Nếu IntelliJ hỏi tạo Run Configuration thì đồng ý.
4. Mở configuration vừa tạo và set active profile là `local`.

Kết quả mong đợi:

- app start ở `http://localhost:8080`
- `http://localhost:8080/actuator/health` trả về `200 OK`

## 6. Tạo Run Configuration Chuẩn

Nên tạo configuration riêng cho từng service bạn hay dùng.

Quy ước nên giữ thống nhất:

- tên có hậu tố `-local`
- main class đúng `*Application`
- active profile là `local`
- working directory là root repo

Gợi ý cấu hình:

- `gateway-local` -> `com.bk.arenax.gateway.ApiGatewayApplication`
- `identity-local` -> `com.bk.arenax.identity.IdentityServiceApplication`
- `access-local` -> `com.bk.arenax.access.AccessServiceApplication`
- `tenant-local` -> `com.bk.arenax.tenant.TenantServiceApplication`
- `subscription-local` -> `com.bk.arenax.subscription.SubscriptionServiceApplication`
- `competition-local` -> `com.bk.arenax.competition.CompetitionServiceApplication`
- `ranking-local` -> `com.bk.arenax.ranking.RankingServiceApplication`

Tùy phiên bản IntelliJ, bạn có thể set profile theo một trong các cách sau:

- điền `local` vào ô `Active profiles`
- hoặc thêm giá trị tương đương `spring.profiles.active=local` vào phần cấu hình Spring Boot

## 7. Chạy 2-3 Service Bằng Nhiều Run Configuration

Làm được hoàn toàn.

Ví dụ bạn đang debug login flow:

1. Run `gateway-local`
2. Run `identity-local`

Ví dụ bạn đang debug match/ranking flow:

1. Run `gateway-local`
2. Run `competition-local`
3. Run `ranking-local`

Sau đó IntelliJ sẽ giữ nhiều process cùng lúc trong `Run` tool window. Bạn có thể:

- stop từng service riêng
- restart từng service riêng
- đặt breakpoint cho từng service riêng

Đây là cách phù hợp khi:

- bạn debug sâu một service chính
- service còn lại chỉ là phụ trợ
- bạn muốn restart đúng một process mà không đụng process khác

## 8. Tạo Compound Configuration

Nếu bạn hay chạy cùng một nhóm service, dùng Compound configuration sẽ tiện hơn.

Ví dụ tạo bộ `gateway-competition-ranking`:

1. Mở `Run | Edit Configurations...`
2. Bấm dấu `+`
3. Chọn `Compound`
4. Đặt tên `gateway-competition-ranking`
5. Add các configuration:
   - `gateway-local`
   - `competition-local`
   - `ranking-local`
6. Save
7. Bấm Run một lần để chạy cả nhóm

Những compound nên tạo sẵn:

- `gateway-identity`
- `gateway-competition-ranking`
- `identity-tenant-access-subscription`

## 9. Service Nào Cần PostgreSQL

`api-gateway` có thể chạy ngay.

Các service cần PostgreSQL local sẵn:

- `identity-service`
- `access-service`
- `tenant-service`
- `subscription-service`
- `competition-service`
- `ranking-service`

Nghĩa là nếu gateway lên được nhưng service business bị fail, lỗi thường là:

- PostgreSQL chưa chạy
- database chưa được tạo
- profile `local` chưa được bật

## 10. Checklist Khi Service Không Lên

Khi một service fail trong IntelliJ, check theo thứ tự này:

1. Có đang chạy đúng `*Application` class không?
2. Có bật profile `local` không?
3. PostgreSQL local có đang chạy không?
4. Database của service đó đã tồn tại chưa?
5. Port mặc định của service có bị chiếm không?

## 11. Gợi Ý Combo Theo Nhu Cầu

Debug login/JWT:

- `api-gateway`
- `identity-service`

Debug onboarding:

- `identity-service`
- `tenant-service`
- `access-service`
- `subscription-service`

Debug match/ranking:

- `api-gateway`
- `competition-service`
- `ranking-service`

Debug một service business qua entry HTTP:

- `api-gateway`
- service business bạn đang sửa

## 12. Khi Nào Dùng IDE, Khi Nào Dùng Script

Dùng IDE khi:

- bạn cần breakpoint
- bạn muốn quan sát log riêng cho từng service
- bạn muốn chạy 2-3 service và restart từng cái độc lập

Dùng script (`bin/run-service`, `bin/run-local-stack`) khi:

- bạn chỉ cần bật nhanh service
- bạn không cần debug từng line
- bạn muốn start background rồi làm việc khác

## 13. Lộ Trình Làm Quen Cho Teammate Mới

Thứ tự dễ tiếp cận nhất hiện tại:

1. start `gateway-local`
2. start `competition-local`
3. start `ranking-local`
4. sau đó mới chuyển sang `identity`, `tenant`, `access`, `subscription`

Lý do là flow competition/ranking hiện dễ nhìn hơn, ít dependency nhận thức hơn flow onboarding.

## 14. Đọc Tiếp

- `docs/development/local-development.md`
- `docs/development/running-services.md`
- `docs/development/running-the-stack.md`
- `docs/onboarding/README.md`
