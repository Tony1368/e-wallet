# Kiến trúc Microservices - E-Wallet HUST

## Sơ đồ Luồng Thanh Toán Đồng Bộ + Bất Đồng Bộ

```
┌──────────┐     ┌──────────────┐     ┌───────────────────┐     ┌─────────────────┐
│ Frontend │────▶│ API Gateway  │────▶│ Payment Service   │────▶│ Fraud Detection │
│ (React)  │     │   :8080      │     │     :8084         │     │    :8086        │
└──────────┘     └──────────────┘     │                   │     └─────────────────┘
                                      │  1. Fraud Check   │           │
                                      │  2. Debit (Redis) │◀──────────┘ OK/BLOCK
                                      │  3. Credit (Redis)│
                                      │  4. Record TX     │
                                      └────────┬──────────┘
                                               │
                          ┌────────────────────┼────────────────────┐
                          │                    │                    │
                          ▼                    ▼                    ▼
                 ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
                 │ Wallet Service  │  │Transaction Svc  │  │                 │
                 │    :8082        │  │    :8083        │  │                 │
                 │                 │  │                 │  │                 │
                 │ Redis (balance) │  │ Save to DB      │  │                 │
                 │ Write-behind→DB │  │ Publish Kafka   │  │                 │
                 └────────┬────────┘  └────────┬────────┘  │                 │
                          │                    │           │                 │
                          ▼                    ▼           │                 │
                 ┌─────────────────┐  ┌─────────────────┐ │                 │
                 │  PostgreSQL     │  │     Kafka       │ │                 │
                 │ wallet_mgmt_db │  │ topic:          │ │                 │
                 │   (port 5433)   │  │ transaction-    │ │                 │
                 └─────────────────┘  │ events          │ │                 │
                                      └────────┬────────┘ │                 │
                                               │           │                 │
                                               ▼           │                 │
                                      ┌─────────────────┐ │                 │
                                      │Accounting Svc   │ │                 │
                                      │    :8085        │ │                 │
                                      │                 │ │                 │
                                      │ @KafkaListener  │ │                 │
                                      │ → JournalEntry  │ │                 │
                                      │   (DEBIT/CREDIT)│ │                 │
                                      └────────┬────────┘ │                 │
                                               │           │                 │
                                               ▼           │                 │
                                      ┌─────────────────┐ │                 │
                                      │  PostgreSQL     │ │                 │
                                      │  accounting_db  │ │                 │
                                      │   (port 5436)   │ │                 │
                                      └─────────────────┘ │                 │
                                                          │                 │
                                                          └─────────────────┘
```

## 6 Databases Độc Lập

| # | Database | Service | Port | Bảng chính |
|---|----------|---------|------|------------|
| 1 | `user_management_db` | user-service | 5432 | user, role, user_session, user_activity |
| 2 | `wallet_management_db` | wallet-service | 5433 | wallet |
| 3 | `transaction_management_db` | transaction-service | 5434 | transaction |
| 4 | `payment_management_db` | payment-service | 5435 | *(stateless - không dùng DB)* |
| 5 | `accounting_db` | accounting-service | 5436 | ledger, journal_entry |
| 6 | `fraud_db` | fraud-detection-service | 5437 | fraud_rule_config |

## Luồng Chi Tiết: Thanh Toán POS

```
POS (Frontend) → API Gateway → Payment Service
                                      │
                                      ├─[1]─▶ Fraud Detection Service (POST /fraud/check)
                                      │         → Kiểm tra max_transaction_amount
                                      │         → Kiểm tra max_daily_transactions
                                      │         → Kiểm tra max_daily_amount
                                      │         → Return: {fraudulent: false}
                                      │
                                      ├─[2]─▶ Wallet Service (POST /wallets/{id}/debit)
                                      │         → Redis INCRBY atomic (trừ balance)
                                      │         → Nếu balance < 0 → rollback → throw error
                                      │         → Write-behind: sync DB mỗi 5s
                                      │
                                      ├─[3]─▶ Wallet Service (POST /wallets/{id}/credit)
                                      │         → Redis INCRBY atomic (cộng balance)
                                      │
                                      └─[4]─▶ Transaction Service (POST /transactions)
                                                → Save transaction to transaction_management_db
                                                → Publish TransactionCompletedEvent to Kafka
                                                        │
                                                        ▼ (Async)
                                                Accounting Service
                                                → @KafkaListener receives event
                                                → Create DEBIT + CREDIT JournalEntry
                                                → Save to accounting_db
```

## Giao Tiếp Giữa Các Service

| Từ | Đến | Phương thức | Mục đích |
|----|-----|-------------|----------|
| Payment → Fraud | REST (sync) | Kiểm tra gian lận trước giao dịch |
| Payment → Wallet | REST (sync) | Debit/Credit balance qua Redis |
| Payment → Transaction | REST (sync) | Ghi nhận giao dịch |
| Transaction → Accounting | **Kafka (async)** | Event-driven: ghi bút toán |
| Wallet → Redis | Cache (sync) | Balance caching, atomic operations |
| Wallet → PostgreSQL | Write-behind (async) | Persist balance mỗi 5s |

## Đảm Bảo Tính Độc Lập

- **Database per Service**: Mỗi service sở hữu DB riêng, không share bảng
- **Fault Isolation**: Nếu accounting-service chết → giao dịch vẫn thành công (Kafka lưu message chờ)
- **Eventual Consistency**: Balance trên Redis → sync DB; Transaction event → Accounting entries
- **Stateless Orchestrator**: Payment Service không có DB, chỉ điều phối các service khác
