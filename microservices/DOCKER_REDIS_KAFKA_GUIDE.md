# Hướng dẫn Cài đặt & Cấu hình Docker cho Redis + Kafka

## Tổng quan Kiến trúc Hạ tầng

```
┌────────────────────────────────────────────────────────────────┐
│                        Docker Network                          │
│                      (ewallet-network)                         │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐   │
│  │   Redis     │  │  Zookeeper  │  │       Kafka         │   │
│  │  :6379      │  │   :2181     │  │      :9092/:29092   │   │
│  └──────┬──────┘  └──────┬──────┘  └──────────┬──────────┘   │
│         │                 │                     │              │
│         │                 └─────────────────────┘              │
│         │                           │                          │
│  ┌──────┴──────┐          ┌─────────┴──────────┐             │
│  │   Wallet    │          │   Transaction      │             │
│  │   Service   │          │   Service (Producer)│             │
│  │   :8082     │          │   :8083            │             │
│  └─────────────┘          └────────────────────┘             │
│                                     │                         │
│                           ┌─────────┴──────────┐             │
│                           │   Accounting       │             │
│                           │   Service (Consumer)│             │
│                           │   :8085            │             │
│                           └────────────────────┘             │
└────────────────────────────────────────────────────────────────┘
```

## Bước 1: Yêu cầu hệ thống

- **Docker Desktop** ≥ 4.20 (Windows/Mac) hoặc Docker Engine ≥ 24.0 (Linux)
- **Docker Compose** ≥ v2.20
- **RAM**: Tối thiểu 8GB (khuyến nghị 16GB)
- **Disk**: Tối thiểu 10GB trống

Kiểm tra phiên bản:
```bash
docker --version
docker compose version
```

## Bước 2: Khởi chạy Hạ tầng (Infrastructure Only)

Trước tiên, chỉ khởi chạy tầng hạ tầng để đảm bảo Redis và Kafka hoạt động ổn:

```bash
cd microservices

# Chạy infrastructure trước
docker compose up -d redis zookeeper kafka user-db wallet-db transaction-db payment-db accounting-db fraud-db
```

### Kiểm tra Redis hoạt động:
```bash
# Kiểm tra container status
docker ps | grep redis

# Test kết nối Redis
docker exec ewallet-redis redis-cli ping
# Kết quả mong đợi: PONG

# Test set/get
docker exec ewallet-redis redis-cli SET test "hello"
docker exec ewallet-redis redis-cli GET test
# Kết quả: "hello"
```

### Kiểm tra Kafka hoạt động:
```bash
# Kiểm tra Zookeeper
docker logs ewallet-zookeeper | tail -5

# Kiểm tra Kafka broker
docker logs ewallet-kafka | tail -10

# Tạo topic thử nghiệm (kiểm tra Kafka sẵn sàng)
docker exec ewallet-kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic test-topic \
  --partitions 1 \
  --replication-factor 1

# Liệt kê topics
docker exec ewallet-kafka kafka-topics --list \
  --bootstrap-server localhost:9092

# Xóa topic thử
docker exec ewallet-kafka kafka-topics --delete \
  --bootstrap-server localhost:9092 \
  --topic test-topic
```

### Kiểm tra tất cả databases:
```bash
# Kiểm tra nhanh tất cả DB
docker exec user-management-db pg_isready -U postgres
docker exec wallet-management-db pg_isready -U postgres
docker exec transaction-management-db pg_isready -U postgres
docker exec payment-management-db pg_isready -U postgres
docker exec accounting-db pg_isready -U postgres
docker exec fraud-db pg_isready -U postgres
```

## Bước 3: Build Microservices

```bash
# Quay lại thư mục microservices root
cd microservices

# Build tất cả services bằng Maven (bỏ qua tests để nhanh)
mvnw.cmd clean package -DskipTests   # Windows
./mvnw clean package -DskipTests      # Linux/Mac
```

## Bước 4: Khởi chạy toàn bộ hệ thống

```bash
# Chạy tất cả services
docker compose up -d --build

# Theo dõi logs real-time
docker compose logs -f

# Hoặc xem log từng service
docker compose logs -f wallet-service
docker compose logs -f transaction-service
docker compose logs -f accounting-service
```

## Bước 5: Xác minh luồng Redis (Wallet Balance Cache)

```bash
# 1. Tạo ví mới qua API
curl -X POST http://localhost:8080/api/v1/wallets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"Test Wallet","iban":"VN123456789","balance":1000000,"userId":1}'

# 2. Kiểm tra balance đã được cache trong Redis
docker exec ewallet-redis redis-cli KEYS "wallet:balance:*"
# Kết quả: wallet:balance:1

docker exec ewallet-redis redis-cli GET "wallet:balance:1"
# Kết quả: 100000000 (= 1,000,000 * 100, đơn vị cents)

# 3. Theo dõi Redis operations real-time
docker exec ewallet-redis redis-cli MONITOR
```

## Bước 6: Xác minh luồng Kafka (Transaction → Accounting)

```bash
# 1. Kiểm tra topic transaction-events đã được tạo tự động
docker exec ewallet-kafka kafka-topics --list \
  --bootstrap-server localhost:9092
# Kết quả phải có: transaction-events

# 2. Mở consumer console để theo dõi messages
docker exec ewallet-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic transaction-events \
  --from-beginning

# 3. Tạo giao dịch để test (ở terminal khác)
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"amount":50000,"fromWalletId":1,"toWalletId":2,"description":"Test Kafka"}'

# 4. Kiểm tra console consumer - sẽ thấy message TransactionCompletedEvent

# 5. Kiểm tra bút toán đã được ghi trong accounting-service
curl http://localhost:8080/api/v1/accounting/journal-entries/<transactionId> \
  -H "Authorization: Bearer <token>"
```

## Bước 7: Quản lý & Troubleshooting

### Các lệnh quản lý thường dùng:

```bash
# Xem trạng thái tất cả containers
docker compose ps

# Restart một service cụ thể
docker compose restart wallet-service

# Xem resource usage
docker stats

# Dừng toàn bộ hệ thống
docker compose down

# Dừng và XÓA toàn bộ data (volumes)
docker compose down -v
```

### Troubleshooting Redis:

| Vấn đề | Nguyên nhân | Giải pháp |
|---------|-------------|-----------|
| `Connection refused :6379` | Redis chưa start | `docker compose up -d redis` → đợi healthcheck pass |
| `OOM command not allowed` | Hết memory Redis | Tăng `maxmemory` trong docker-compose |
| Balance không sync | Write-behind chưa chạy | Kiểm tra logs wallet-service: `syncBalancesToDatabase` |

```bash
# Kiểm tra Redis memory usage
docker exec ewallet-redis redis-cli INFO memory | grep used_memory_human

# Xem tất cả keys
docker exec ewallet-redis redis-cli KEYS "*"

# Flush all (CHỈ dùng khi test!)
docker exec ewallet-redis redis-cli FLUSHALL
```

### Troubleshooting Kafka:

| Vấn đề | Nguyên nhân | Giải pháp |
|---------|-------------|-----------|
| `Connection to node -1 failed` | Kafka chưa ready | Đợi 30s sau khi Kafka start (start_period) |
| `Topic not found` | Auto-create chưa chạy | Kafka tự tạo khi Producer gửi message đầu tiên |
| Consumer không nhận message | Group ID sai | Kiểm tra `group-id: accounting-group` trong application.yml |
| Message bị duplicate | Consumer chưa commit | Kiểm tra `ack-mode: record` |

```bash
# Kiểm tra consumer groups
docker exec ewallet-kafka kafka-consumer-groups --list \
  --bootstrap-server localhost:9092

# Xem offset lag
docker exec ewallet-kafka kafka-consumer-groups --describe \
  --bootstrap-server localhost:9092 \
  --group accounting-group

# Reset offset về đầu (khi cần reprocess)
docker exec ewallet-kafka kafka-consumer-groups --reset-offsets \
  --bootstrap-server localhost:9092 \
  --group accounting-group \
  --topic transaction-events \
  --to-earliest --execute
```

### Troubleshooting Startup Order:

Nếu service khởi động trước khi Kafka sẵn sàng:
```bash
# Restart service sau khi infrastructure healthy
docker compose restart transaction-service accounting-service
```

## Cấu hình Environment Variables

### Redis (wallet-service):
| Variable | Mô tả | Default |
|----------|--------|---------|
| `SPRING_DATA_REDIS_HOST` | Redis hostname | `localhost` |
| `SPRING_DATA_REDIS_PORT` | Redis port | `6379` |
| `REDIS_HOST` | Fallback hostname | `localhost` |
| `REDIS_PORT` | Fallback port | `6379` |

### Kafka (transaction-service, accounting-service):
| Variable | Mô tả | Default |
|----------|--------|---------|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka broker address | `localhost:9092` |
| `KAFKA_BOOTSTRAP_SERVERS` | Fallback address | `localhost:9092` |

> **Lưu ý**: Trong Docker network, service giao tiếp qua `kafka:29092` (internal listener). Từ host machine, truy cập qua `localhost:9092` (external listener).

## Cấu hình Production Recommendations

### Redis:
```yaml
# Thêm password cho production
command: redis-server --appendonly yes --requirepass <password> --maxmemory 1gb
```

### Kafka:
```yaml
# Tăng replication cho production (cần ≥ 3 brokers)
KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3
KAFKA_DEFAULT_REPLICATION_FACTOR: 3
```

### Resource Limits:
```yaml
# Thêm vào từng service trong docker-compose
deploy:
  resources:
    limits:
      cpus: '1.0'
      memory: 512M
    reservations:
      cpus: '0.25'
      memory: 256M
```
