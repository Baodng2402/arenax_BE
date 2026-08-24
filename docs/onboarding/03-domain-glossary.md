> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# 03. Domain Glossary

File này là glossary onboarding ngắn. Nếu định nghĩa nào lệch với source hiện tại, lấy `docs/overview.md` và docs của service liên quan làm chuẩn.

## User

Identity của người dùng trong hệ thống.

User được tạo ở `identity-service` và có các trạng thái như `PENDING` (mới register, chưa verify email), `ACTIVE`, `SUSPENDED`, `DEACTIVATED`.

Identity root của user là `users.id` (UUID); mọi service khác reference user bằng `userId`, không dùng email.

## Account

Đơn vị sở hữu tài nguyên theo tenant boundary, thuộc `tenant-service`.

Có `PERSONAL` (tạo mặc định khi onboarding) và `TEAM` (workspace, tạo qua `POST /api/v1/accounts/workspaces`).

## Membership

Liên kết giữa user và account trong `tenant-service`, mang role trong account (`OWNER`, `MEMBER`).

User thuộc nhiều account qua nhiều memberships (ví dụ personal account + vài workspace).

## Role

Nhóm quyền trong `identity-service` (RBAC đã merge vào Identity; `access-service` không tồn tại).

Role không phải global authorization object dùng chung toàn repo; nó thuộc boundary của Identity.

## Permission

Quyền chi tiết trong `identity-service`, ví dụ `MATCH:CREATE`, `MATCH:JOIN`, `RANKING:READ`.

## Role Assignment

Gán một role cho một `userId` trong một `accountId` cụ thể (bảng `role_assignments`, unique trên `(user_id, account_id, role_code)`).

Authorization được thiết kế theo tenant/account scope, không phải role toàn cục không ngữ cảnh.

## Authorization (JWT Claims)

`roles` và `permissions` trong JWT được đọc từ chính database của `identity-service` khi login (qua RbacService), không cần gọi service nào khác.

## User Identifier

Cách user đăng nhập, thuộc `identity-service` (bảng `user_identifiers`).

Hiện hỗ trợ type `EMAIL`: một user có thể có nhiều email, mỗi email verified/primary riêng; email dùng để login hoặc reset password phải được verify. Chỉ có một primary email tại một thời điểm; đổi primary sẽ sync `users.email` legacy.

## Username

Handle công khai tùy chọn của user (`users.username`, unique, nullable). Không phải identity root và không dùng để login.

## Subscription

State gói dịch vụ theo account trong `subscription-service`.

Plan hiện có `FREE`, `PRO`, `TEAM`; status `ACTIVE`, `CANCELLED`. Entitlements được suy ra từ plan khi trả response.

## Sport

Loại môn thể thao trong `competition-service`, ví dụ football.

## Match

Trận đấu trong `competition-service`.

Match own lifecycle create -> join -> complete. Ranking không được chỉnh match state.

## Participant

Người tham gia match, được lưu bằng `userId` trong `competition-service` thay vì JPA relation sang identity.

## Ranking Projection

Projection hiện tại của điểm ELO cho một user trong `ranking-service`.

Projection này được build từ event `competition.match-completed.v1`.

## Outbox Event

Row local trong database của producer service, dùng để lưu event đã phát sinh cùng transaction với business state.

Nó là nền cho message publishing đáng tin cậy. Outbox hiện có cột `published_at`; relay đánh dấu sau khi publish thành công lên topic exchange `arenax.events`.

## Correlation ID

ID dùng để nối nhiều event trong cùng một business workflow (producer hay dùng `correlationId` = chính khóa business như userId/accountId/matchId).

## Read Next

- `04-data-and-integration-map.md` nếu bạn muốn nối các term này với ownership/integration thực tế.
- `../services/README.md` nếu bạn đã biết service mình sắp sửa.
