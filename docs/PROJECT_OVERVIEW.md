# TỔNG QUAN HỆ THỐNG VÍ ĐIỆN TỬ (E-WALLET)

## 1. Mô tả dự án

### 1.1. Giới thiệu
Hệ thống Ví điện tử HUST là ứng dụng full-stack quản lý ví điện tử và giao dịch nội bộ doanh nghiệp. Hệ thống cho phép nhân viên nhận điểm thưởng, chuyển điểm, thanh toán tại cửa hàng, và đổi phần thưởng. Phía quản trị cung cấp chức năng kế toán, phát hiện gian lận, và quản lý chi nhánh.

Dự án được xây dựng theo hai kiến trúc để so sánh hiệu năng:
- **Kiến trúc nguyên khối (Monolithic)**: Single JAR, shared database
- **Kiến trúc vi dịch vụ (Microservices)**: 6 services độc lập, event-driven

### 1.2. Chức năng chính

| STT | Module | Chức năng |
|-----|--------|-----------|
| 1 | Quản lý người dùng | Đăng ký, đăng nhập (JWT), phân quyền RBAC, theo dõi phiên đăng nhập |
| 2 | Quản lý ví | Tạo/sửa/xóa ví, cấp điểm batch (Excel/CSV), quản lý theo chi nhánh |
| 3 | Giao dịch | Chuyển điểm, nạp điểm, rút điểm, tra cứu lịch sử |
| 4 | Thanh toán | Thanh toán POS, đổi thưởng (redeem), hoàn điểm (refund) |
| 5 | Kế toán | Bút toán tự động (Journal Entry), sổ cái, kết chuyển ERP, xuất báo cáo CSV |
| 6 | Phát hiện gian lận | Giới hạn số tiền, velocity check, anomaly detection, geo-velocity |

### 1.3. Vai trò người dùng

| Role | Quyền hạn |
|------|-----------|
| ROLE_ADMIN | Toàn quyền quản trị hệ thống |
| ROLE_ACCOUNTANT | Xem tất cả ví, quản lý bút toán, kết chuyển ERP |
| ROLE_MANAGER | Quản lý cửa hàng, duyệt hoàn điểm, xem ví chi nhánh mình |
| ROLE_CASHIER | Thu ngân POS, tạo yêu cầu hoàn điểm, xem ví chi nhánh |
| ROLE_CUSTOMER | Nhân viên — xem ví cá nhân, chuyển điểm, lịch sử giao dịch |

---

## 2. Cơ sở dữ liệu

Hệ thống microservices sử dụng mô hình **Database per Service** với 6 PostgreSQL instances riêng biệt.

### 2.1. User Management Database (`user_management_db` — Port 5432)

#### Bảng `user`
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGINT | PK | Mã người dùng (sequence) |
| first_name | VARCHAR(50) | NOT NULL | Họ |
| last_name | VARCHAR(50) | NOT NULL | Tên |
| username | VARCHAR(20) | NOT NULL, UNIQUE | Tên đăng nhập |
| email | VARCHAR(50) | NOT NULL, UNIQUE | Email |
| password | VARCHAR(100) | NOT NULL | Mật khẩu (BCrypt hash) |
| branch_id | BIGINT | | Mã chi nhánh |

#### Bảng `role`
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGINT | PK | Mã vai trò |
| type | VARCHAR(20) | NOT NULL, UNIQUE | Tên vai trò (ROLE_ADMIN, ROLE_CUSTOMER, ...) |

#### Bảng `user_role`
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| user_id | BIGINT | PK, FK → user | Mã người dùng |
| role_id | BIGINT | PK, FK → role | Mã vai trò |

#### Bảng `user_session`
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGINT | PK | Mã phiên |
| session_id | VARCHAR(255) | NOT NULL, UNIQUE | UUID phiên đăng nhập |
| login_time | TIMESTAMP | NOT NULL | Thời điểm đăng nhập |
| logout_time | TIMESTAMP | | Thời điểm đăng xuất |
| ip_address | VARCHAR(45) | | Địa chỉ IP |
| latitude | VARCHAR(20) | | Vĩ độ GPS |
| longitude | VARCHAR(20) | | Kinh độ GPS |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | Phiên còn hoạt động |
| user_id | BIGINT | FK → user | Mã người dùng |

#### Bảng `user_activity`
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGINT | PK | Mã hoạt động |
| activity_id | VARCHAR(255) | NOT NULL, UNIQUE | UUID hoạt động |
| activity_time | TIMESTAMP | NOT NULL | Thời điểm |
| activity_type | VARCHAR(50) | NOT NULL | Loại (LOGIN, LOGOUT, TRANSFER,...) |
| description | VARCHAR(500) | | Mô tả chi tiết |
| amount | NUMERIC | | Số tiền (nếu có) |
| is_successful | BOOLEAN | NOT NULL, DEFAULT true | Thành công / thất bại |
| error_message | VARCHAR(500) | | Thông báo lỗi |
| user_id | BIGINT | FK → user | Mã người dùng |
| session_id | BIGINT | FK → user_session | Mã phiên |

---

### 2.2. Wallet Management Database (`wallet_management_db` — Port 5433)

#### Bảng `branch`
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGINT | PK | Mã chi nhánh |
| name | VARCHAR(255) | NOT NULL | Tên chi nhánh |
| code | VARCHAR(50) | NOT NULL, UNIQUE | Mã viết tắt (HQ, TDN, BK) |
| address | VARCHAR(500) | | Địa chỉ |
| created_at | TIMESTAMP | NOT NULL | Ngày tạo |

#### Bảng `wallet`
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGINT | PK | Mã ví |
| name | VARCHAR(255) | NOT NULL | Tên ví |
| iban | VARCHAR(255) | NOT NULL, UNIQUE | Số tài khoản IBAN |
| balance | NUMERIC(19,2) | NOT NULL, DEFAULT 0 | Số dư hiện tại |
| user_id | BIGINT | NOT NULL | Mã chủ ví |
| created_at | TIMESTAMP | NOT NULL | Ngày tạo |
| status | VARCHAR(50) | NOT NULL, DEFAULT 'ACTIVE' | Trạng thái (ACTIVE/CLOSED) |
| bank_info | VARCHAR(500) | | Thông tin ngân hàng |
| branch_id | BIGINT | FK → branch | Mã chi nhánh |

---

### 2.3. Transaction Management Database (`transaction_management_db` — Port 5434)

#### Bảng `transaction`
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGINT | PK | Mã giao dịch |
| amount | NUMERIC(19,2) | NOT NULL | Số tiền |
| description | VARCHAR(50) | | Mô tả |
| created_at | TIMESTAMP | NOT NULL | Thời gian tạo |
| reference_number | UUID | NOT NULL, UNIQUE | Mã tham chiếu |
| status | VARCHAR(20) | NOT NULL | Trạng thái (SUCCESS/PENDING/FAILED) |
| from_wallet_id | BIGINT | NOT NULL | Ví nguồn |
| to_wallet_id | BIGINT | NOT NULL | Ví đích |
| type_id | BIGINT | NOT NULL | Loại giao dịch (1=Chuyển, 4=Nạp, 5=Rút, 6=Đổi, 7=Hoàn, 8=Batch) |

---

### 2.4. Payment Management Database (`payment_management_db` — Port 5435)

#### Bảng `refund_request`
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGINT | PK | Mã yêu cầu |
| transaction_id | VARCHAR(255) | NOT NULL | Mã giao dịch cần hoàn |
| wallet_id | BIGINT | NOT NULL | Ví cửa hàng |
| amount | NUMERIC(19,2) | NOT NULL | Số tiền hoàn |
| reason | VARCHAR(500) | | Lý do hoàn |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | Trạng thái (PENDING/APPROVED/REJECTED) |
| requested_by | VARCHAR(100) | | Người yêu cầu |
| approved_by | VARCHAR(100) | | Người duyệt |
| created_at | TIMESTAMP | NOT NULL | Ngày tạo |
| updated_at | TIMESTAMP | | Ngày cập nhật |

---

### 2.5. Accounting Database (`accounting_db` — Port 5436)

#### Bảng `ledger`
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGINT | PK | Mã sổ cái |
| name | VARCHAR(100) | NOT NULL, UNIQUE | Tên sổ cái (GENERAL) |
| description | VARCHAR(255) | | Mô tả |
| created_at | TIMESTAMP | NOT NULL | Ngày tạo |

#### Bảng `journal_entry`
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGINT | PK | Mã bút toán |
| transaction_id | UUID | NOT NULL | Mã tham chiếu giao dịch |
| ledger_id | BIGINT | FK → ledger | Sổ cái |
| from_wallet_id | BIGINT | NOT NULL | Ví nguồn |
| to_wallet_id | BIGINT | NOT NULL | Ví đích |
| amount | NUMERIC(19,2) | NOT NULL | Số tiền |
| entry_type | VARCHAR(10) | NOT NULL | Loại bút toán (DEBIT/CREDIT) |
| description | VARCHAR(255) | | Mô tả |
| transaction_type | VARCHAR(30) | | Phân loại (CHUYEN_DIEM, NAP_DIEM, RUT_DIEM, DOI_THUONG, HOAN_DIEM, CAP_DIEM_BATCH) |
| created_at | TIMESTAMP | NOT NULL | Thời gian tạo |
| erp_transferred | BOOLEAN | NOT NULL, DEFAULT false | Đã kết chuyển ERP |
| erp_transferred_at | TIMESTAMP | | Thời điểm kết chuyển |

---

### 2.6. Fraud Detection Database (`fraud_db` — Port 5437)

#### Bảng `fraud_rule_config`
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGINT | PK | Mã cấu hình |
| rule_name | VARCHAR(100) | NOT NULL, UNIQUE | Tên bộ quy tắc |
| max_transaction_amount | NUMERIC(19,2) | NOT NULL | Số tiền tối đa / giao dịch |
| max_daily_transactions | INTEGER | NOT NULL | Số giao dịch tối đa / ngày |
| max_daily_amount | NUMERIC(19,2) | NOT NULL | Tổng tiền tối đa / ngày |
| max_transactions_per_minute | INTEGER | NOT NULL, DEFAULT 10 | Số GD tối đa / phút (velocity) |
| velocity_window_seconds | INTEGER | NOT NULL, DEFAULT 60 | Khung thời gian velocity (giây) |
| geo_velocity_minutes | INTEGER | NOT NULL, DEFAULT 30 | Thời gian tối thiểu giữa 2 vị trí |
| geo_velocity_enabled | BOOLEAN | NOT NULL, DEFAULT true | Bật/tắt kiểm tra vị trí |
| anomaly_amount_multiplier | NUMERIC(5,2) | NOT NULL, DEFAULT 3.00 | Hệ số phát hiện bất thường |
| anomaly_enabled | BOOLEAN | NOT NULL, DEFAULT true | Bật/tắt phát hiện bất thường |
| enabled | BOOLEAN | NOT NULL, DEFAULT true | Bật/tắt toàn bộ quy tắc |
| created_at | TIMESTAMP | NOT NULL | Ngày tạo |

---

## 3. Cách thức triển khai (Microservices)

### 3.1. Yêu cầu hệ thống

| Thành phần | Phiên bản tối thiểu |
|-----------|---------------------|
| Docker Engine | 24.x |
| Docker Compose | v2.x |
| RAM | ≥ 8 GB (khuyến nghị 16 GB) |
| Disk | ≥ 10 GB trống |
| Ports khả dụng | 3000, 5432-5437, 6379, 8080-8086, 9092 |

### 3.2. Cấu trúc thư mục

```
e-wallet/
├── microservices/
│   ├── user-management-service/      # Service quản lý người dùng
│   ├── wallet-management-service/    # Service quản lý ví
│   ├── transaction-management-service/ # Service quản lý giao dịch
│   ├── payment-management-service/   # Service thanh toán
│   ├── accounting-service/           # Service kế toán
│   ├── fraud-detection-service/      # Service phát hiện gian lận
│   ├── api-gateway/                  # API Gateway
│   ├── common-service/               # Thư viện dùng chung
│   └── docker-compose.yml            # File triển khai
├── frontend/                         # React Frontend
├── monolithic-baseline/              # Phiên bản nguyên khối (so sánh)
└── docker-compose-monolithic.yml     # Triển khai monolithic
```

### 3.3. Các bước triển khai

**Bước 1: Clone dự án**
```bash
git clone <repository-url>
cd e-wallet/microservices
```

**Bước 2: Khởi động toàn bộ hệ thống (1 lệnh duy nhất)**
```bash
docker compose up -d --build
```

**Bước 3: Kiểm tra trạng thái**
```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

**Bước 4: Truy cập ứng dụng**
- Frontend: http://localhost:3000
- API Gateway: http://localhost:8080/api/v1
- Swagger (Wallet): http://localhost:8082/swagger-ui.html

### 3.4. Danh sách containers (17 containers)

| # | Container | Image | Port | Chức năng |
|---|-----------|-------|------|-----------|
| 1 | ewallet-frontend | node:18-alpine | 3000 | Giao diện người dùng |
| 2 | api-gateway | eclipse-temurin:21 | 8080 | Định tuyến, xác thực, rate limit |
| 3 | user-management-service | eclipse-temurin:21 | 8081 | Quản lý người dùng, JWT |
| 4 | wallet-management-service | eclipse-temurin:21 | 8082 | Quản lý ví, cấp điểm |
| 5 | transaction-management-service | eclipse-temurin:21 | 8083 | Ghi nhận giao dịch |
| 6 | payment-management-service | eclipse-temurin:21 | 8084 | Thanh toán, hoàn điểm |
| 7 | accounting-service | eclipse-temurin:21 | 8085 | Kế toán, bút toán |
| 8 | fraud-detection-service | eclipse-temurin:21 | 8086 | Phát hiện gian lận |
| 9 | user-management-db | postgres:16-alpine | 5432 | DB người dùng |
| 10 | wallet-management-db | postgres:16-alpine | 5433 | DB ví |
| 11 | transaction-management-db | postgres:16-alpine | 5434 | DB giao dịch |
| 12 | payment-management-db | postgres:16-alpine | 5435 | DB thanh toán |
| 13 | accounting-db | postgres:16-alpine | 5436 | DB kế toán |
| 14 | fraud-db | postgres:16-alpine | 5437 | DB gian lận |
| 15 | ewallet-redis | redis:alpine | 6379 | Cache số dư + Rate Limiting |
| 16 | ewallet-kafka | cp-kafka:7.5.0 | 9092 | Message broker (event streaming) |
| 17 | ewallet-zookeeper | cp-zookeeper:7.5.0 | 2181 | Kafka coordination |

### 3.5. Giao tiếp giữa các service

| Loại | Công nghệ | Mô tả |
|------|-----------|-------|
| Đồng bộ (Sync) | REST / RestTemplate | Payment → Wallet, Transaction, Fraud |
| Bất đồng bộ (Async) | Apache Kafka | Transaction → Accounting (event: transaction-events) |
| Cache | Redis | Wallet balance (write-behind), API Gateway rate limiting |

### 3.6. Công nghệ sử dụng

| Lớp | Công nghệ | Phiên bản |
|-----|-----------|-----------|
| Backend | Spring Boot | 3.4.1 |
| Runtime | Java (Eclipse Temurin) | 21 LTS |
| Frontend | React | 18 |
| UI Framework | Material UI | 5 |
| Database | PostgreSQL | 16 |
| Cache | Redis | 7 (Alpine) |
| Message Broker | Apache Kafka | 7.5.0 (Confluent) |
| API Gateway | Spring Cloud Gateway | 2024.0 |
| Security | Spring Security + JWT | jjwt 0.12.6 |
| ORM | Hibernate / Spring Data JPA | 6.6.4 |
| DB Migration | Flyway | 10.20 |
| Build | Maven | 3.9 |
| Container | Docker / Docker Compose | 24.x / v2 |

---

## 4. Tài khoản thử nghiệm

| Username | Vai trò | Chi nhánh |
|----------|---------|-----------|
| admin | ROLE_ADMIN | Hội sở chính (HQ) |
| thailq | ROLE_ADMIN | Hội sở chính (HQ) |
| ketoan | ROLE_ACCOUNTANT | Hội sở chính (HQ) |
| quanly | ROLE_MANAGER | Chi nhánh Trần Đại Nghĩa (TDN) |
| pos | ROLE_CASHIER | Chi nhánh Trần Đại Nghĩa (TDN) |
| nhanvien | ROLE_CUSTOMER | Chi nhánh Trần Đại Nghĩa (TDN) |
| congthanh | ROLE_USER | Chi nhánh Bách Khoa (BK) |

---

## 5. Ví điện tử mẫu

| ID | Tên ví | IBAN | Chủ sở hữu | Chi nhánh |
|----|--------|------|-------------|-----------|
| 1 | Apple Wallet | GB33BUKB20201555555555 | admin | HQ |
| 2 | Samsung Wallet | GB94BARC10201530093459 | admin | HQ |
| 3 | Cửa hàng HUST - Trần Đại Nghĩa | GB29NWBK60161331926819 | pos | TDN |
| 4 | Lê Thành Công | DE75512108001245126199 | nhanvien | TDN |
| 5 | Quản lý cửa hàng HUST | FR7630006000011234567890189 | quanly | TDN |
| 6 | Phòng kế toán - Hội sở | AT483200000012345864 | ketoan | HQ |
