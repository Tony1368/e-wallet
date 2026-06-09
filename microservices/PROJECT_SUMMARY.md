# Tổng hợp Project E-Wallet HUST

## 1. Công nghệ sử dụng

### Backend (Microservices)
| Công nghệ | Phiên bản | Vai trò |
|-----------|-----------|---------|
| Java | 21 | Ngôn ngữ chính |
| Spring Boot | 3.4.1 | Framework |
| Spring Cloud Gateway | 4.2.0 | API Gateway |
| Spring Data JPA | 3.4.1 | ORM / Database access |
| Spring Data Redis | 3.4.1 | Cache balance ví |
| Spring Kafka | 3.4.1 | Event-driven messaging |
| Spring Security + JWT | 6.4.x | Xác thực, phân quyền |
| PostgreSQL | 16 | Database chính (6 instances) |
| Redis | Alpine | Cache & atomic operations |
| Apache Kafka | 7.5.0 (Confluent) | Message broker |
| Zookeeper | 7.5.0 (Confluent) | Kafka cluster management |
| Flyway | 10.20.1 | Database migration |
| Lombok | 1.18.36 | Code generation |
| MapStruct | 1.6.3 | DTO mapping |
| Maven | Multi-module | Build tool |
| Docker | Compose v2 | Container orchestration |

### Frontend
| Công nghệ | Phiên bản | Vai trò |
|-----------|-----------|---------|
| React | 18.2 | UI Framework |
| Material UI (MUI) | 5.11 | Component library |
| React Router DOM | 6.9 | Routing |
| Axios | 1.6 | HTTP client |
| qrcode.react | 4.2 | QR Code generation |
| Notistack | 3.0 | Toast notifications |
| ApexCharts | 3.37 | Charts & dashboard |
| React Hook Form | 7.43 | Form handling |
| Node.js | 18 | Runtime |

---

## 2. Cấu trúc Project

### Tổng quan thư mục
```
e-wallet/
├── microservices/                    # Backend (7 services + gateway + common)
│   ├── api-gateway/                  # Spring Cloud Gateway (:8080)
│   ├── common-service/               # Shared DTOs, exceptions
│   ├── user-management-service/      # Quản lý user, auth, tracking (:8081)
│   ├── wallet-management-service/    # Quản lý ví, Redis cache (:8082)
│   ├── transaction-management-service/ # Ghi giao dịch, Kafka producer (:8083)
│   ├── payment-management-service/   # Orchestrator thanh toán (:8084)
│   ├── accounting-service/           # Kế toán, Kafka consumer (:8085)
│   ├── fraud-detection-service/      # Phát hiện gian lận (:8086)
│   ├── docker-compose.yml            # 14 containers
│   └── pom.xml                       # Parent POM
├── frontend/                         # React SPA (:3000)
│   ├── src/pages/                    # Các màn hình
│   ├── src/services/                 # API clients
│   ├── src/layouts/                  # Layout + Navigation
│   └── package.json
└── README.md
```

### 6 Databases độc lập
| Database | Port | Service | Bảng chính |
|----------|------|---------|------------|
| user_management_db | 5432 | user-service | user, role, user_session, user_activity |
| wallet_management_db | 5433 | wallet-service | wallet |
| transaction_management_db | 5434 | transaction-service | transaction |
| payment_management_db | 5435 | payment-service | *(stateless, không dùng DB)* |
| accounting_db | 5436 | accounting-service | ledger, journal_entry |
| fraud_db | 5437 | fraud-detection-service | fraud_rule_config |

### Infrastructure containers
| Container | Image | Port |
|-----------|-------|------|
| ewallet-redis | redis:alpine | 6379 |
| ewallet-zookeeper | confluentinc/cp-zookeeper:7.5.0 | 2181 |
| ewallet-kafka | confluentinc/cp-kafka:7.5.0 | 9092 |

---

## 3. Chức năng theo Service

### 3.1 User Management Service (:8081)
- Đăng ký / Đăng nhập (JWT Token)
- Phân quyền: ROLE_ADMIN, ROLE_ACCOUNTANT, ROLE_CUSTOMER, ROLE_CASHIER, ROLE_MANAGER
- Quản lý phiên đăng nhập (session tracking)
- Ghi log hoạt động người dùng (activity tracking)
- Phát hiện thay đổi IP/location bất thường
- API quản trị: danh sách users, sessions, activities

### 3.2 Wallet Management Service (:8082)
- CRUD ví điện tử (tạo, sửa, xóa, đổi trạng thái)
- Tra cứu ví theo IBAN, userId
- Cache balance trên Redis (atomic INCRBY)
- Debit/Credit qua Redis (high-throughput)
- Write-behind: đồng bộ Redis → PostgreSQL mỗi 5 giây
- Admin: xem tất cả ví

### 3.3 Transaction Management Service (:8083)
- Ghi nhận giao dịch (amount, from/to wallet, type, status)
- Tra cứu theo id, reference number, userId
- Kafka Producer: publish TransactionCompletedEvent khi giao dịch SUCCESS
- Admin: xem tất cả giao dịch, tìm kiếm theo username

### 3.4 Payment Management Service (:8084)
- **Orchestrator** - điều phối luồng thanh toán:
  1. Gọi Fraud Detection kiểm tra gian lận
  2. Gọi Wallet Service debit/credit (Redis atomic)
  3. Gọi Transaction Service ghi nhận giao dịch
- Chuyển điểm ví-ví (transfer)
- Nạp điểm (add funds)
- Rút điểm (withdraw)
- Đổi thưởng (redeem rewards)
- Stateless: không có database riêng

### 3.5 Accounting Service (:8085)
- Kafka Consumer: lắng nghe topic `transaction-events`
- Tự động tạo bút toán DEBIT + CREDIT cho mỗi giao dịch
- Quản lý Sổ cái (Ledger) và Bút toán (JournalEntry)
- API tra cứu bút toán theo transactionId

### 3.6 Fraud Detection Service (:8086)
- API kiểm tra giao dịch (`POST /fraud/check`)
- Quy tắc: max số tiền/giao dịch, max số giao dịch/ngày, max tổng tiền/ngày
- Admin: cấu hình rules (GET/PUT /admin/fraud-config)

### 3.7 API Gateway (:8080)
- Định tuyến request đến đúng service
- 8 routes: auth, users, tracking, wallets, transactions, payments, accounting, fraud

---

## 4. Chức năng Frontend (theo màn hình)

### 4.1 Xác thực
| Màn hình | Route | Chức năng |
|----------|-------|-----------|
| Login | /login | Đăng nhập bằng username/password |
| Signup | /signup | Đăng ký tài khoản mới |

### 4.2 Người dùng chung
| Màn hình | Route | Chức năng |
|----------|-------|-----------|
| Dashboard | / | Tổng quan thống kê |
| Ví điện tử | /wallets | Danh sách ví, đổi trạng thái |
| Thêm ví | /wallets/new | Tạo ví mới (IBAN) |
| Giao dịch | /transfers | Chuyển điểm, nạp, rút, tạo QR, đổi thưởng |
| Lịch sử | /transactions | Xem lịch sử giao dịch |

### 4.3 Cổng Nhân viên (Mobile-first)
| Màn hình | Route | Chức năng |
|----------|-------|-----------|
| Employee Portal | /employee | Chọn ví → hiển thị QR tương ứng, xem số dư, giao dịch gần đây |

### 4.4 Giả lập POS (Thu ngân)
| Màn hình | Route | Chức năng |
|----------|-------|-----------|
| POS Simulator | /pos | Nhập mã NV + số tiền → thanh toán, yêu cầu hoàn điểm |

### 4.5 Quản lý Cửa hàng
| Màn hình | Route | Chức năng |
|----------|-------|-----------|
| Store Manager | /store-manager | Polling 3s nhận yêu cầu hoàn điểm, phê duyệt/từ chối, quỹ voucher |

### 4.6 Kế toán & Quản trị
| Màn hình | Route | Chức năng |
|----------|-------|-----------|
| Accounting | /accounting | Upload Excel cấp điểm theo lô, xem bút toán, lọc ngày, xuất Excel |
| Quản lý GD | /admin/transactions | Xem tất cả giao dịch, tìm kiếm |
| Quản lý Ví | /admin/wallets | Xem tất cả ví, đổi trạng thái |
| User Tracking | /admin/tracking | Sessions, activities, lọc username |
| Fraud Config | /admin/fraud-config | Cấu hình rules phát hiện gian lận |

---

## 5. Luồng giao tiếp giữa Services

```
Frontend → Gateway → Payment Service
                          │
                          ├──[sync]──▶ Fraud Detection (kiểm tra)
                          ├──[sync]──▶ Wallet Service (debit/credit qua Redis)
                          └──[sync]──▶ Transaction Service (ghi DB)
                                              │
                                              └──[async/Kafka]──▶ Accounting Service (bút toán)
```

| Từ → Đến | Giao thức | Mô tả |
|-----------|-----------|-------|
| Payment → Fraud | REST sync | Kiểm tra trước khi trừ tiền |
| Payment → Wallet | REST sync | Debit/Credit atomic trên Redis |
| Payment → Transaction | REST sync | Ghi transaction record |
| Transaction → Accounting | Kafka async | Event TransactionCompletedEvent |
| Wallet → Redis | Lettuce | Cache balance, INCRBY atomic |
| Wallet → PostgreSQL | Write-behind | Sync mỗi 5 giây |

---

## 6. Phân quyền (RBAC)

| Role | Truy cập |
|------|----------|
| ROLE_ADMIN | Tất cả màn hình |
| ROLE_ACCOUNTANT | Kế toán, Quản lý GD, Quản lý Ví |
| ROLE_MANAGER | Quản lý Cửa hàng |
| ROLE_CASHIER | Giả lập POS |
| ROLE_USER / ROLE_CUSTOMER | Ví, Giao dịch, Cổng Nhân viên |

---

## 7. Docker Compose (14 containers)

```
6 PostgreSQL + 1 Redis + 1 Zookeeper + 1 Kafka + 6 Services + 1 Gateway + 1 Frontend = 17 containers
```

Khởi chạy: `docker compose up -d --build`
