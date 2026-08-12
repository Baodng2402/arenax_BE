# 06. First Week Guide

## Mục Tiêu Của Người Mới Trong Tuần Đầu

Không phải viết feature lớn ngay.

Mục tiêu đúng là:

- hiểu service boundary
- hiểu onboarding flow và ranking flow
- biết chỗ nào là source of truth
- biết cách thêm một vertical slice mà không phá kiến trúc

## Ngày 1: Đọc Và Chạy Test

Đọc theo thứ tự:

1. `README.md`
2. `docs/onboarding/README.md`
3. `docs/architecture/service-boundaries.md`
4. `docs/architecture/conventions.md`
5. `docs/services/identity.md`
6. `docs/services/competition.md`
7. `docs/services/ranking.md`

Sau đó chạy:

```bash
./gradlew test
```

Mục tiêu là biết repo đang xanh ở đâu trước khi đụng code.

## Ngày 2: Trace Một Flow End-To-End

Chọn một flow:

- onboarding user mới
- match completion -> ranking update

Khi trace, luôn trả lời được 4 câu hỏi:

- request hoặc event bắt đầu ở đâu
- state được lưu vào bảng nào
- event nào được ghi ra outbox
- service tiếp theo consume gì

## Ngày 3: Đọc Theo Service Ownership

Với mỗi service, xác nhận 3 thứ:

- nó own bảng nào
- nó produce event nào
- nó consume event nào

Nếu không trả lời được 3 câu này, chưa nên implement feature mới ở service đó.

## Ngày 4: Làm Một Thay Đổi Nhỏ

Loại thay đổi phù hợp cho người mới:

- thêm một validation rule đơn giản
- thêm một integration test còn thiếu
- thêm một event side-effect assertion
- cập nhật docs cho một capability đã có

Tránh ngay tuần đầu:

- thêm service mới
- thêm shared module business
- wiring distributed runtime lớn nếu chưa hiểu flow hiện tại

## Ngày 5: Tập Viết Một Vertical Slice Đúng Pattern

Checklist ngắn:

1. xác định service own dữ liệu gì
2. xác định endpoint hoặc event vào là gì
3. viết test fail trước
4. thêm entity/repository/migration tối thiểu
5. thêm service logic tối thiểu
6. thêm controller hoặc handler
7. chạy focused test
8. chạy `./gradlew test`
9. update docs liên quan

## Các Câu Hỏi Tự Kiểm Tra

Nếu bạn đã onboard xong, bạn nên trả lời được:

- vì sao không được add code mới vào root `src/`
- vì sao Identity giữ authorization projection local
- vì sao Ranking không đọc DB của Competition
- vì sao event payload Java class không được share giữa services
- vì sao outbox bắt buộc để có outbox relay + listener adapter chạy trên RabbitMQ (`compose.yaml`)

## Khi Bị Kẹt

Đọc theo thứ tự ưu tiên:

1. `docs/onboarding/*.md`
2. `docs/architecture/*.md`
3. `docs/services/*.md`
4. test integration của service liên quan

Nếu vẫn mơ hồ, quay lại câu hỏi gốc:

"service nào own state này?"

Đó thường là điểm mở nút nhanh nhất trong repo này.
