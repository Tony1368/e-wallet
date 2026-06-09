# Hướng dẫn Test Tải E-Wallet bằng JMeter

## 1. Cài đặt JMeter

1. Tải Apache JMeter: https://jmeter.apache.org/download_jmeter.cgi
2. Giải nén vào thư mục (VD: `C:\apache-jmeter-5.6.3`)
3. Thêm vào PATH: `C:\apache-jmeter-5.6.3\bin`

## 2. Chuẩn bị dữ liệu test

Trước khi chạy test, đảm bảo hệ thống có dữ liệu:

```bash
# Tạo 2 ví test (thay token bằng token thực sau khi login)
curl -X POST http://localhost:8080/api/v1/wallets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"Ví Test 1","iban":"VN0001","balance":10000000,"userId":1}'

curl -X POST http://localhost:8080/api/v1/wallets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"Ví Test 2","iban":"VN0002","balance":10000000,"userId":1}'
```

## 3. Chạy Test

### Chạy GUI (xem kết quả trực quan):
```bash
jmeter -t d:\SourceCode\e-wallet\jmeter-tests\ewallet-load-test.jmx
```

### Chạy Non-GUI (khuyến nghị cho test tải thực):
```bash
jmeter -n -t d:\SourceCode\e-wallet\jmeter-tests\ewallet-load-test.jmx -l results\results.jtl -e -o results\html-report
```

## 4. Cấu hình Test Scenarios

### File: `ewallet-load-test.jmx`

| # | Test Case | Threads | Ramp-up | Duration | Mục đích |
|---|-----------|---------|---------|----------|----------|
| 01 | Login | 100 | 10s | 60s | Đo throughput xác thực JWT |
| 02 | Query Wallets | 200 | 10s | 60s | Đo read performance (Redis cache) |
| 03 | Transfer Funds | 50 | 10s | 60s | Đo write performance (luồng đầy đủ) |
| 04 | Query Transactions | 200 | 10s | 60s | Đo truy vấn lịch sử |
| 05 | Add Funds | 50 | 5s | 60s | Đo nạp điểm |

### Luồng mỗi test:
```
Login (lấy JWT) → Gọi API nghiệp vụ (với Bearer token)
```

## 5. Tùy chỉnh tham số

Mở file `.jmx` trong JMeter GUI, thay đổi:

| Tham số | Vị trí | Mặc định | Gợi ý test |
|---------|--------|----------|------------|
| `BASE_URL` | User Defined Variables | localhost | IP server |
| `PORT` | User Defined Variables | 8080 | Port gateway |
| `ThreadGroup.num_threads` | Từng Thread Group | 50-200 | Tăng dần: 50→100→200→500 |
| `ThreadGroup.ramp_time` | Từng Thread Group | 10 | Giữ 10-30s |
| `ThreadGroup.duration` | Từng Thread Group | 60 | 60-300s |
| username/password | Body login | admin/johnd@e | Thay bằng user thực |
| fromWalletIban/toWalletIban | Body transfer | VN0001/VN0002 | IBAN ví đã tạo |

## 6. Kịch bản test tải nâng cao

### Spike Test (Đột biến):
- Threads: 500, Ramp-up: 5s, Duration: 30s
- Mục đích: Kiểm tra hệ thống khi đột ngột tăng tải

### Endurance Test (Bền vững):
- Threads: 100, Ramp-up: 30s, Duration: 600s (10 phút)
- Mục đích: Phát hiện memory leak, connection pool exhaustion

### Stress Test (Giới hạn):
- Tăng dần threads: 100 → 200 → 500 → 1000 → 2000
- Mục đích: Tìm điểm gãy (breakpoint) của hệ thống

## 7. Đọc kết quả

### Summary Report (quan trọng nhất):
| Metric | Ý nghĩa | Ngưỡng tốt |
|--------|----------|-------------|
| **Throughput** | Requests/second | > 500 TPS (login), > 1000 TPS (query) |
| **Average** | Response time trung bình | < 200ms |
| **90% Line** | 90% requests nhanh hơn giá trị này | < 500ms |
| **99% Line** | 99% requests nhanh hơn giá trị này | < 1000ms |
| **Error %** | Tỉ lệ lỗi | < 1% |

### Xem HTML Report:
```bash
# Sau khi chạy non-GUI, mở:
start results\html-report\index.html
```

## 8. Lưu ý

- **Không chạy test tải trên production**
- Đảm bảo Docker containers đủ RAM (ít nhất 8GB cho Docker)
- Tắt `View Results Tree` listener khi chạy test thực (tốn RAM)
- Chạy JMeter trên máy khác với server nếu có thể
- Monitor Docker: `docker stats` trong terminal khác
