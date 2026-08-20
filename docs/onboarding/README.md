> **Tài liệu tham khảo** — File này là reference chi tiết. Bắt đầu đọc từ [README](../../README.md) → [docs/overview.md](../overview.md).

# ArenaX Onboarding Guide

Chào mừng bạn đến với dự án ArenaX Backend! Tài liệu này định hướng nhanh cho developer mới tham gia dự án.

## 🚀 3 Bước Bắt Đầu Nhanh

1. **Đọc tổng quan kiến trúc hệ thống:**
   - `docs/onboarding/01-system-tour.md` (Tổng quan các service)
   - `docs/onboarding/02-core-flows.md` (Luồng xử lý cốt lõi)
2. **Khởi chạy môi trường local:**
   - Xem hướng dẫn chi tiết tại `docs/development/running-the-stack.md`
   - Hoặc chạy nhanh hạ tầng:
     ```bash
     docker compose up -d
     ```
3. **Tuân thủ quy chuẩn code:**
   - `docs/architecture/conventions.md`
   - `docs/architecture/service-boundaries.md`

## 📚 Mục Lục Tài Liệu

- **Onboarding:**
  - `01-system-tour.md` - Sơ đồ và chức năng các microservices
  - `02-core-flows.md` - Luồng nghiệp vụ chính
  - `03-domain-glossary.md` - Thuật ngữ domain
  - `04-data-and-integration-map.md` - Bản đồ dữ liệu và tích hợp
- **Development:**
  - `running-the-stack.md` - Hướng dẫn chạy local stack
  - `intellij-setup.md` - Cấu hình IntelliJ IDEA
  - `testing.md` - Quy chuẩn viết test
- **Architecture:**
  - `service-boundaries.md` - Ranh giới giữa các service
  - `conventions.md` - Convention lập trình chung
