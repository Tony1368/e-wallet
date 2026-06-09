# Cơ sở dữ liệu E-Wallet - Tổng hợp bảng và cột

## 1. user_management_db (Port 5432)

### Bảng: user
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGINT | PK | Mã người dùng |
| first_name | VARCHAR(50) | NOT NULL | Họ |
| last_name | VARCHAR(50) | NOT NULL | Tên |
| username | VARCHAR(20) | NOT NULL, UNIQUE | Tên đăng nhập |
| email | VARCHAR(50) | NOT NULL, UNIQUE | Email |
| password | VARCHAR(100) | NOT NULL | Mật khẩu (đã mã hóa) |

### Bảng: role
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGINT | PK | Mã vai trò |
| type | VARCHAR(20) | NOT NULL, UNIQUE | Loại vai trò (ROLE_ADMIN, ROLE_USER,...) |

### Bảng: user_role
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| user_id | BIGINT | PK, FK → user(id) | Mã người dùng |
| role_id | BIGINT | PK, FK → role(id) | Mã vai trò |

### Bảng: user_session
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGINT | PK | Mã phiên |
| session_id | VARCHAR(255) | NOT NULL, UNIQUE | UUID phiên đăng nhập |
| user_id | BIGINT | NOT NULL, FK → user(id) | Mã người dùng |
| login_time | TIMESTAMP | NOT NULL, DEFAULT NOW | Thời gian đăng nhập |
| logout_time | TIMESTAMP | NULLABLE | Thời gian đăng xuất |
| ip_address | VARCHAR(45) | NULLABLE | Địa chỉ IP |
| user_agent | VARCHAR(500) | NULLABLE | User-Agent trình duyệt |
| device_type | VARCHAR(100) | NULLABLE | Loại thiết bị |
| browser | VARCHAR(100) | NULLABLE | Tên trình duyệt |
| operating_system | VARCHAR(100) | NULLABLE | Hệ điều hành |
| country | VARCHAR(100) | NULLABLE | Quốc gia |
| city | VARCHAR(100) | NULLABLE | Thành phố |
| region | VARCHAR(100) | NULLABLE | Vùng/Tỉnh |
| latitude | VARCHAR(20) | NULLABLE | Vĩ độ |
| longitude | VARCHAR(20) | NULLABLE | Kinh độ |
| timezone | VARCHAR(50) | NULLABLE | Múi giờ |
| is_active | BOOLEAN | NOT NULL, DEFAULT TRUE | Phiên đang hoạt động |

### Bảng: user_activity
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGINT | PK | Mã hoạt động |
| activity_id | VARCHAR(255) | NOT NULL, UNIQUE | UUID hoạt động |
| user_id | BIGINT | NOT NULL, FK → user(id) | Mã người dùng |
| session_id | BIGINT | NULLABLE, FK → user_session(id) | Phiên liên quan |
| activity_type | VARCHAR(50) | NOT NULL | Loại (LOGIN, LOGOUT, TRANSFER, WITHDRAW, ADD_FUNDS) |
| activity_time | TIMESTAMP | NOT NULL | Thời gian hoạt động |
| description | VARCHAR(500) | NULLABLE | Mô tả |
| amount | NUMERIC | NULLABLE | Số tiền (nếu là giao dịch tài chính) |
| from_wallet_iban | VARCHAR(34) | NULLABLE | IBAN ví nguồn |
| to_wallet_iban | VARCHAR(34) | NULLABLE | IBAN ví đích |
| ip_address | VARCHAR(45) | NULLABLE | Địa chỉ IP |
| user_agent | VARCHAR(500) | NULLABLE | User-Agent |
| device_type | VARCHAR(100) | NULLABLE | Loại thiết bị |
| browser | VARCHAR(100) | NULLABLE | Trình duyệt |
| operating_system | VARCHAR(100) | NULLABLE | Hệ điều hành |
| country | VARCHAR(100) | NULLABLE | Quốc gia |
| city | VARCHAR(100) | NULLABLE | Thành phố |
| region | VARCHAR(100) | NULLABLE | Vùng |
| latitude | VARCHAR(20) | NULLABLE | Vĩ độ |
| longitude | VARCHAR(20) | NULLABLE | Kinh độ |
| timezone | VARCHAR(50) | NULLABLE | Múi giờ |
| is_successful | BOOLEAN | NOT NULL, DEFAULT TRUE | Thành công hay thất bại |
| error_message | VARCHAR(500) | NULLABLE | Thông báo lỗi (nếu thất bại) |

---

## 2. wallet_management_db (Port 5433)

### Bảng: wallet
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGSERIAL | PK | Mã ví |
| name | VARCHAR(255) | NOT NULL | Tên ví |
| iban | VARCHAR(255) | NOT NULL, UNIQUE | Số tài khoản IBAN |
| balance | NUMERIC(19,2) | NOT NULL, DEFAULT 0.00 | Số dư |
| user_id | BIGINT | NOT NULL | Mã chủ sở hữu |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW | Ngày tạo |
| status | VARCHAR(50) | NOT NULL, DEFAULT 'ACTIVE' | Trạng thái (ACTIVE, CLOSED) |
| bank_info | VARCHAR(500) | NULLABLE | Thông tin ngân hàng liên kết |

**Index**: idx_wallet_user_id, idx_wallet_iban

---

## 3. transaction_management_db (Port 5434)

### Bảng: transaction
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGSERIAL | PK | Mã giao dịch |
| amount | NUMERIC(19,2) | NOT NULL | Số tiền |
| description | VARCHAR(50) | NULLABLE | Diễn giải |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW | Thời gian tạo |
| reference_number | UUID | NOT NULL, UNIQUE | Mã tham chiếu duy nhất |
| status | VARCHAR(20) | NOT NULL | Trạng thái (SUCCESS, PENDING, FAILED) |
| from_wallet_id | BIGINT | NOT NULL | ID ví nguồn |
| to_wallet_id | BIGINT | NOT NULL | ID ví đích |
| type_id | BIGINT | NOT NULL | Loại giao dịch (1=Transfer, 4=Add, 5=Withdraw, 6=Redeem) |

**Index**: idx_transaction_from_wallet, idx_transaction_to_wallet, idx_transaction_reference

---

## 4. accounting_db (Port 5436)

### Bảng: ledger
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGSERIAL | PK | Mã sổ cái |
| name | VARCHAR(100) | NOT NULL, UNIQUE | Tên sổ cái (VD: GENERAL) |
| description | VARCHAR(255) | NULLABLE | Mô tả |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW | Ngày tạo |

### Bảng: journal_entry
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGSERIAL | PK | Mã bút toán |
| transaction_id | UUID | NOT NULL | Mã giao dịch tham chiếu |
| ledger_id | BIGINT | NOT NULL, FK → ledger(id) | Sổ cái |
| from_wallet_id | BIGINT | NOT NULL | ID ví nguồn |
| to_wallet_id | BIGINT | NOT NULL | ID ví đích |
| amount | NUMERIC(19,2) | NOT NULL | Số tiền |
| entry_type | VARCHAR(10) | NOT NULL | Loại bút toán (DEBIT / CREDIT) |
| description | VARCHAR(255) | NULLABLE | Mô tả |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW | Thời gian tạo |

**Index**: idx_journal_entry_transaction_id

---

## 5. fraud_db (Port 5437)

### Bảng: fraud_rule_config
| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|-----|---------------|-----------|-------|
| id | BIGSERIAL | PK | Mã cấu hình |
| rule_name | VARCHAR(100) | NOT NULL, UNIQUE | Tên quy tắc |
| max_transaction_amount | NUMERIC(19,2) | NOT NULL | Giới hạn số tiền tối đa/giao dịch |
| max_daily_transactions | INTEGER | NOT NULL | Giới hạn số giao dịch tối đa/ngày |
| max_daily_amount | NUMERIC(19,2) | NOT NULL | Giới hạn tổng tiền tối đa/ngày |
| enabled | BOOLEAN | NOT NULL, DEFAULT TRUE | Bật/tắt quy tắc |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW | Ngày tạo |

---

## 6. Redis Cache (Port 6379)

| Key Pattern | Kiểu | Mô tả |
|-------------|------|-------|
| `wallet:balance:{walletId}` | STRING (số nguyên - cents) | Số dư ví × 100 (để dùng atomic INCRBY) |

---

## 7. Kafka Topics

| Topic | Producer | Consumer | Payload |
|-------|----------|----------|---------|
| `transaction-events` | transaction-service | accounting-service | `{transactionId, amount, fromWalletId, toWalletId, description}` |

---

## Sơ đồ quan hệ tổng quan

```
user_management_db:
  user ──1:N──▶ user_role ◀──N:1── role
  user ──1:N──▶ user_session ──1:N──▶ user_activity

wallet_management_db:
  wallet (user_id tham chiếu logic đến user.id)

transaction_management_db:
  transaction (from_wallet_id, to_wallet_id tham chiếu logic đến wallet.id)

accounting_db:
  ledger ──1:N──▶ journal_entry (transaction_id tham chiếu logic đến transaction.reference_number)

fraud_db:
  fraud_rule_config (độc lập, không FK)
```

> **Lưu ý**: Giữa các database khác nhau không có Foreign Key vật lý (đúng kiến trúc microservice). Các tham chiếu chéo chỉ là tham chiếu logic qua ID/IBAN.
