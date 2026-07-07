# Phương án kiểm thử Saga Compensation dưới tải

## 1. Mục tiêu kiểm thử

Chứng minh 3 điều:
1. **Không rò rỉ số dư (Data Leak = 0)**: Tổng balance hệ thống trước và sau test phải bằng nhau
2. **Compensation kịp thời**: Dưới tải cao, rollback xảy ra ngay trong cùng request (< response time)
3. **Không tranh chấp tài nguyên**: Redis atomic operations đảm bảo consistency dù concurrent cao

## 2. Kịch bản kiểm thử

### Kịch bản 1: Transaction Service chết → Compensation hoàn cả 2 ví

| Thông số | Giá trị |
|----------|---------|
| Concurrent users | 50 |
| Tổng requests | 500 |
| Fault injection | `docker pause transaction-management-service` |
| Kỳ vọng | 100% requests fail + compensation + Delta = 0 |

**Luồng:**
```
Debit ví A (thành công trên Redis)
  → Credit ví B (thành công trên Redis)
    → Record Transaction (FAIL - service paused)
      → COMPENSATION: Credit ví A + Debit ví B
        → Trả 500 cho client
```

### Kịch bản 2: Wallet Service timeout → Compensation hoàn debit

| Thông số | Giá trị |
|----------|---------|
| Concurrent users | 30 |
| Tổng requests | 200 |
| Fault injection | `tc netem delay 10000ms` trên wallet-service |
| Client timeout | 5s (credit sẽ timeout) |
| Kỳ vọng | Requests timeout + compensation + Delta = 0 |

**Luồng:**
```
Debit ví A (thành công - trước khi inject fault)
  → Credit ví B (TIMEOUT - wallet-service chậm 10s)
    → COMPENSATION: Credit ví A (hoàn tiền)
      → Trả 500 cho client
```

### Kịch bản 3: Mixed load (50% thành công + 50% thất bại)

| Thông số | Giá trị |
|----------|---------|
| Concurrent users | 100 |
| Tổng requests | 1000 |
| Fault injection | Ngắt transaction-service sau 500 requests |
| Kỳ vọng | 500 thành công + 500 compensation + Delta = 0 |

## 3. Chỉ số đo lường

| Chỉ số | Công thức | Ngưỡng chấp nhận |
|--------|-----------|-------------------|
| **Data Leak (Delta)** | `Tổng_sau - Tổng_trước` | = 0 |
| **Compensation Rate** | `Số_compensation / Số_fail × 100%` | = 100% |
| **Compensation Latency** | `Response_time_fail - Response_time_success` | < 50ms overhead |
| **Throughput under failure** | `Total_requests / Duration` | > 50 req/s |

## 4. Cách chạy

```bash
cd d:\SourceCode\e-wallet\microservices\test-scripts

# Kịch bản 1: Transaction Service chết
bash test-saga-compensation.sh

# Kịch bản 2: Credit timeout
bash test-saga-credit-failure.sh
```

## 5. Kết quả mẫu kỳ vọng

```
============================================================
  KẾT QUẢ KIỂM THỬ
============================================================
  Delta (rò rỉ số dư): 0
  ✅ PASS: Không có rò rỉ số dư tài chính
  ✅ Saga Compensation hoạt động đúng dưới tải 50 concurrent
  ✅ Tổng số dư hệ thống bảo toàn (invariant giữ vững)

  Throughput: 156 req/s
  Compensation rate: 100%
============================================================
```

## 6. Giải thích tại sao Delta = 0

```
Invariant: balance(A) + balance(B) = CONSTANT

Happy path:
  balance(A) -= amount    (debit)
  balance(B) += amount    (credit)
  → Tổng không đổi ✓

Compensation path (credit fail):
  balance(A) -= amount    (debit thành công)
  balance(B) += amount    (credit FAIL)
  balance(A) += amount    (compensation: hoàn debit)
  → Tổng không đổi ✓

Compensation path (transaction fail):
  balance(A) -= amount    (debit thành công)
  balance(B) += amount    (credit thành công)
  Transaction.save()      (FAIL)
  balance(A) += amount    (compensation: hoàn debit)
  balance(B) -= amount    (compensation: thu hồi credit)
  → Tổng không đổi ✓
```

Redis INCRBY/DECRBY là atomic O(1) → không race condition dù 50 threads đồng thời.
