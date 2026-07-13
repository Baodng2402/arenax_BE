# 03. Domain Glossary

## User

Identity của người dùng trong hệ thống.

Trong trạng thái hiện tại, user được tạo ở `identity-service` và có thể ở các trạng thái như `PROVISIONING` hoặc `ACTIVE`.

## Account

Đơn vị sở hữu tài nguyên theo tenant boundary.

Hiện tại onboarding mặc định tạo một `PERSONAL` account cho user mới.

## Membership

Liên kết giữa user và account trong `tenant-service`.

Trong onboarding hiện tại, user mới nhận membership `OWNER` cho personal account của chính họ.

## Role

Nhóm quyền trong `access-service`, ví dụ role mặc định `USER`.

Role không phải global authorization object dùng chung toàn repo; nó thuộc boundary của Access.

## Permission

Quyền chi tiết trong `access-service`, ví dụ `MATCH:CREATE`, `MATCH:JOIN`, `RANKING:READ`.

## Role Assignment

Gán một role cho một `userId` trong một `accountId` cụ thể.

Đây là chỗ rất quan trọng: authorization đang được thiết kế theo tenant/account scope, không phải role toàn cục không ngữ cảnh.

## Authorization Projection

Bản sao local trong `identity-service` chứa roles và permissions đã được materialize để đưa vào JWT khi login.

Projection này giúp downstream service validate token locally mà không phải gọi ngược sang Access cho mỗi request.

## Onboarding Progress

State nội bộ của `identity-service` để biết một user mới đã nhận đủ tín hiệu từ các service khác hay chưa.

Identity chỉ activate user khi progress cho thấy authorization và subscription đã sẵn sàng.

## Subscription

State gói dịch vụ theo account trong `subscription-service`.

Hiện tại mới có flow provision mặc định `FREE`.

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

Nó là nền cho message publishing đáng tin cậy, dù broker runtime chưa wiring xong.

## Correlation ID

ID dùng để nối nhiều event trong cùng một business workflow.

Ví dụ onboarding flow dùng correlation ID để Identity biết các tín hiệu completion thuộc về cùng một user registration.
