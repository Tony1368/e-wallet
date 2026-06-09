# Danh gia Quy trinh Thanh toan Vong kin - E-Wallet HUST

## 1. Quan tri han muc phuc loi nhan vien

### Trang thai: DA CO
- Moi nhan vien duoc gan vi dien tu (wallet) voi han muc (balance)
- Quan tri vien/Ke toan co the nap diem theo lo (batch) qua file Excel
- Kiem soat trang thai vi: ACTIVE / CLOSED
- Gioi han so tien toi da moi giao dich, moi ngay (fraud_rule_config)

### Luong nghiep vu:
```
Ke toan upload Excel → Wallet Service credit tung vi (Redis atomic)
→ Transaction Service ghi nhan → Kafka → Accounting Service tao but toan
```

---

## 2. Kiem soat gian lan (Fraud Detection)

### Trang thai: DA CO - 6 QUY TAC
| # | Quy tac | Tham so | Mac dinh |
|---|---------|---------|----------|
| 1 | Gioi han so tien/GD | max_transaction_amount | 50,000,000 |
| 2 | Gioi han so GD/ngay | max_daily_transactions | 100 |
| 3 | Gioi han tong tien/ngay | max_daily_amount | 200,000,000 |
| 4 | Tan suat GD (velocity) | max_transactions_per_minute | 10 GD/phut |
| 5 | Vi tri dia ly (geo-velocity) | geo_velocity_minutes | 30 phut / >50km |
| 6 | So tien bat thuong (anomaly) | anomaly_amount_multiplier | 3x trung binh |

### Luong kiem tra:
```
Payment Service → POST /fraud/check (truoc khi tru tien)
→ Fraud Service kiem tra 6 quy tac
→ Neu vi pham → Block giao dich, tra ve ly do
→ Neu hop le → Tiep tuc debit/credit
```

---

## 3. Phan quyen 5 cap vai tro

### Trang thai: DA CO
| Vai tro | Quyen truy cap |
|---------|---------------|
| ROLE_ADMIN | Tat ca chuc nang, cau hinh fraud, tracking |
| ROLE_ACCOUNTANT | Ke toan, quan ly GD, quan ly vi, cap diem lo |
| ROLE_MANAGER | Quan ly cua hang, phe duyet hoan diem, quy voucher |
| ROLE_CASHIER | Gia lap POS, thanh toan, yeu cau hoan diem |
| ROLE_CUSTOMER | Vi dien tu, giao dich, cong nhan vien, QR code |

### Thuc thi:
- Backend: Spring Security + JWT, @PreAuthorize theo role
- Frontend: NavSection filter menu theo role, ProtectedRoute kiem tra

---

## 4. Write-behind Cache (Redis)

### Trang thai: DA CO
```
┌─────────────────────────────────────────────────┐
│ Luong thanh toan (< 5ms)                        │
│                                                 │
│ 1. Redis INCRBY wallet:balance:{id} (atomic)    │
│ 2. Kiem tra balance < 0 → rollback              │
│ 3. Return thanh cong                            │
└─────────────────────────────────────────────────┘
           │
           ▼ (moi 5 giay - khong anh huong luong chinh)
┌─────────────────────────────────────────────────┐
│ @Scheduled(fixedDelay = 5000)                   │
│ syncBalancesToDatabase()                        │
│ → Scan tat ca key wallet:balance:*              │
│ → Cap nhat PostgreSQL                           │
└─────────────────────────────────────────────────┘
```

### Dac diem:
- Don vi luu tru: cents (x100) de dung atomic INCRBY (chi ho tro integer)
- Kiem tra insufficient: neu balance < 0 sau INCRBY → rollback ngay
- Khong lock DB trong luong thanh toan → throughput cao

---

## 5. Man hinh POS - Hoan diem

### Trang thai: FRONTEND CO, BACKEND CHUA CO ENDPOINT

#### Da co:
- Frontend `/pos`: Thu ngan nhap ma NV + so tien → Thanh toan
- Frontend: Nut "YEU CAU HOAN DIEM" → goi `/payments/refund-request`
- Frontend `/store-manager`: Polling 3s nhan yeu cau, nut Phe duyet/Tu choi

#### Chua co (can bo sung o backend):
- Endpoint `POST /payments/refund-request` (tao yeu cau)
- Endpoint `GET /payments/refund-requests/pending` (danh sach cho duyet)
- Endpoint `POST /payments/refund-requests/{id}/approve` (phe duyet)
- Endpoint `POST /payments/refund-requests/{id}/reject` (tu choi)
- Bang `refund_request` trong payment_management_db

#### Luong mong muon:
```
Thu ngan (CASHIER) → Yeu cau hoan diem
→ Luu trang thai PENDING
→ Manager/Accountant/Admin nhan thong bao (polling)
→ Phe duyet → Wallet credit lai → Transaction ghi nhan
→ Tu choi → Cap nhat trang thai REJECTED
```

---

## 6. Cong Ke toan - Ket chuyen ERP

### Trang thai: FRONTEND CO, BACKEND CHUA CO LOGIC KET CHUYEN

#### Da co:
- Upload Excel cap diem lo
- Xem but toan (journal_entry) tu Kafka consumer
- Loc theo ngay, phan trang
- Nut "Xuat Excel" (goi export API)
- Nut "Ket chuyen ERP" (hien disabled)

#### Chua co (can bo sung):
- Cot `erp_transferred` va `erp_transferred_at` trong bang `journal_entry`
- Endpoint `POST /accounting/erp-transfer` (danh dau cac but toan da ket chuyen)
- Logic: chon ngay → danh dau tat ca but toan cua ngay do la "da ket chuyen"
- Frontend: nut "Ket chuyen ERP" enabled, hien trang thai da/chua ket chuyen

---

## 7. Tom tat Gap va Khuyen nghi

| Hang muc | Trang thai | Muc do uu tien |
|----------|-----------|----------------|
| Write-behind Redis | ✅ Hoan thanh | - |
| Fraud 6 quy tac | ✅ Hoan thanh | - |
| Phan quyen 5 role | ✅ Hoan thanh | - |
| Luong thanh toan POS | ✅ Hoan thanh | - |
| Cap diem lo Excel | ✅ Frontend co | Backend can endpoint |
| Hoan diem + Phe duyet | ⚠️ Frontend co | Backend can 4 endpoint + DB |
| Ket chuyen ERP | ⚠️ Frontend co | Backend can 1 endpoint + 2 cot DB |
| Xuat Excel but toan | ⚠️ Frontend co | Backend can export endpoint |
