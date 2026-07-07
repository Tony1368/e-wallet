#!/bin/bash
# ============================================================
# KIEM THU KAFKA EVENTUAL CONSISTENCY LAG
# Muc tieu: Do thoi gian accounting-service tieu thu het backlog
# Kafka sau dot tai dot bien, dua so cai ke toan ve trang thai
# khop toan hoan toan (transaction count = journal entry count).
# ============================================================

API_URL="http://localhost:8080"
TRANSACTION_SERVICE_URL="http://localhost:8083"
ACCOUNTING_SERVICE_URL="http://localhost:8085"
TOTAL_REQUESTS=1000
CONCURRENT=50
WALLET_A_IBAN="AL35202111090000000001234567"
WALLET_B_IBAN="AD1400080001001234567890"
TRANSFER_AMOUNT=10
USERNAME="nhanvien"
PASSWORD="johnd@e"
MAX_WAIT=60  # seconds max to wait for convergence

echo "============================================================"
echo "  KIEM THU KAFKA EVENTUAL CONSISTENCY LAG"
echo "============================================================"
echo "  Muc tieu: Do thoi gian accounting catch up sau burst"
echo "  Load: $TOTAL_REQUESTS transactions / $CONCURRENT concurrent"
echo ""

# --- Login ---
echo "[Buoc 0] Dang nhap..."
TOKEN=$(curl -s -X POST "$API_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}" | python -c "import sys,json; print(json.load(sys.stdin)['token'])" 2>/dev/null)

if [ -z "$TOKEN" ]; then
  echo "FAIL: Khong the dang nhap"
  exit 1
fi
echo "  Token: ${TOKEN:0:20}..."

# --- Tam nang fraud limits de test khong bi block ---
echo ""
echo "[Buoc 0.5] Tam tat fraud detection de test khong bi block..."
curl -s -X PUT "http://localhost:8086/api/v1/admin/fraud-config/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"ruleName":"enabled","value":"false"}' > /dev/null
echo "  Done (fraud enabled=false)"

# --- Buoc 1: Ghi nhan so luong TRUOC test ---
echo ""
echo "[Buoc 1] Ghi nhan so luong truoc test..."

TX_COUNT_BEFORE=$(curl -s "$TRANSACTION_SERVICE_URL/api/v1/transactions/count" \
  -H "Authorization: Bearer $TOKEN" | python -c "import sys,json; print(json.load(sys.stdin).get('count',0))" 2>/dev/null)
JOURNAL_COUNT_BEFORE=$(curl -s "$ACCOUNTING_SERVICE_URL/api/v1/accounting/count" \
  -H "Authorization: Bearer $TOKEN" | python -c "import sys,json; print(json.load(sys.stdin).get('transactionCount',0))" 2>/dev/null)

echo "  Transactions (truoc): $TX_COUNT_BEFORE"
echo "  Journal entries / 2 (truoc): $JOURNAL_COUNT_BEFORE"

# --- Buoc 2: Ban tai dot bien ---
echo ""
echo "[Buoc 2] Gui $TOTAL_REQUESTS transfer dong thoi ($CONCURRENT threads)..."
echo "  Moi request tao 1 transaction + publish 1 Kafka event"
echo ""

START_TIME=$(date +%s)

seq 1 $TOTAL_REQUESTS | xargs -P $CONCURRENT -I {} curl -s -o /dev/null -w "%{http_code}\n" \
  -X POST "$API_URL/api/v1/payments/transfer" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"fromWalletIban\":\"$WALLET_A_IBAN\",\"toWalletIban\":\"$WALLET_B_IBAN\",\"amount\":$TRANSFER_AMOUNT,\"typeId\":1,\"description\":\"Kafka lag test\"}" \
  > /tmp/kafka_lag_results.txt

END_TIME=$(date +%s)
LOAD_DURATION=$((END_TIME - START_TIME))
if [ "$LOAD_DURATION" -eq 0 ]; then LOAD_DURATION=1; fi

SUCCESS_COUNT=$(cat /tmp/kafka_lag_results.txt | grep -c "^201$" || true)
FAIL_COUNT=$(cat /tmp/kafka_lag_results.txt | grep -cv "^201$" || true)

echo "  Hoan thanh trong: ${LOAD_DURATION}s"
echo "  Thanh cong (201): $SUCCESS_COUNT"
echo "  That bai: $FAIL_COUNT"
echo "  Throughput: $((TOTAL_REQUESTS / LOAD_DURATION)) req/s"

# --- Buoc 3: Do so luong NGAY SAU tai ---
echo ""
echo "[Buoc 3] Do so luong ngay sau tai (Kafka chua tieu thu het)..."

TX_COUNT_AFTER=$(curl -s "$TRANSACTION_SERVICE_URL/api/v1/transactions/count" \
  -H "Authorization: Bearer $TOKEN" | python -c "import sys,json; print(json.load(sys.stdin).get('count',0))" 2>/dev/null)
JOURNAL_COUNT_IMMEDIATE=$(curl -s "$ACCOUNTING_SERVICE_URL/api/v1/accounting/count" \
  -H "Authorization: Bearer $TOKEN" | python -c "import sys,json; print(json.load(sys.stdin).get('transactionCount',0))" 2>/dev/null)

NEW_TX=$((TX_COUNT_AFTER - TX_COUNT_BEFORE))
NEW_JOURNAL_IMMEDIATE=$((JOURNAL_COUNT_IMMEDIATE - JOURNAL_COUNT_BEFORE))
BACKLOG_IMMEDIATE=$((NEW_TX - NEW_JOURNAL_IMMEDIATE))

echo "  Transactions moi tao: $NEW_TX"
echo "  Journal entries da xu ly: $NEW_JOURNAL_IMMEDIATE"
echo "  Backlog (chua xu ly): $BACKLOG_IMMEDIATE"

# --- Buoc 4: Polling cho den khi accounting catch up ---
echo ""
echo "[Buoc 4] Doi accounting-service tieu thu het backlog..."
echo "  Polling moi 1s (timeout: ${MAX_WAIT}s)..."

CONVERGENCE_START=$(date +%s)
CONVERGED=false

for i in $(seq 1 $MAX_WAIT); do
  JOURNAL_COUNT_NOW=$(curl -s "$ACCOUNTING_SERVICE_URL/api/v1/accounting/count" \
    -H "Authorization: Bearer $TOKEN" | python -c "import sys,json; print(json.load(sys.stdin).get('transactionCount',0))" 2>/dev/null)
  
  NEW_JOURNAL_NOW=$((JOURNAL_COUNT_NOW - JOURNAL_COUNT_BEFORE))
  REMAINING=$((NEW_TX - NEW_JOURNAL_NOW))
  
  if [ "$REMAINING" -le 0 ]; then
    CONVERGED=true
    CONVERGENCE_END=$(date +%s)
    CONVERGENCE_LAG=$((CONVERGENCE_END - CONVERGENCE_START))
    echo "  [${i}s] Converged! Journal=$NEW_JOURNAL_NOW / Tx=$NEW_TX (Remaining=0)"
    break
  fi
  
  # Print progress every 2s
  if [ $((i % 2)) -eq 0 ]; then
    echo "  [${i}s] Journal=$NEW_JOURNAL_NOW / Tx=$NEW_TX (Remaining=$REMAINING)"
  fi
  
  sleep 1
done

if [ "$CONVERGED" = false ]; then
  CONVERGENCE_LAG=$MAX_WAIT
  echo "  TIMEOUT: Khong hoi tu sau ${MAX_WAIT}s"
fi

# --- Buoc 5: Xac nhan cuoi cung ---
echo ""
echo "[Buoc 5] Xac nhan trang thai cuoi cung..."

TX_FINAL=$(curl -s "$TRANSACTION_SERVICE_URL/api/v1/transactions/count" \
  -H "Authorization: Bearer $TOKEN" | python -c "import sys,json; print(json.load(sys.stdin).get('count',0))" 2>/dev/null)
JOURNAL_FINAL=$(curl -s "$ACCOUNTING_SERVICE_URL/api/v1/accounting/count" \
  -H "Authorization: Bearer $TOKEN" | python -c "import sys,json; print(json.load(sys.stdin).get('transactionCount',0))" 2>/dev/null)

NEW_TX_FINAL=$((TX_FINAL - TX_COUNT_BEFORE))
NEW_JOURNAL_FINAL=$((JOURNAL_FINAL - JOURNAL_COUNT_BEFORE))
FINAL_DIFF=$((NEW_TX_FINAL - NEW_JOURNAL_FINAL))

echo "  Transactions tao moi: $NEW_TX_FINAL"
echo "  Journal entries xu ly: $NEW_JOURNAL_FINAL"
echo "  Chenh lech cuoi: $FINAL_DIFF"

# --- Buoc 6: Ket qua ---
echo ""
echo "============================================================"
echo "  KET QUA KIEM THU KAFKA EVENTUAL CONSISTENCY LAG"
echo "============================================================"
echo "  Load: $TOTAL_REQUESTS requests / $CONCURRENT concurrent"
echo "  Thanh cong: $SUCCESS_COUNT transactions"
echo "  Load duration: ${LOAD_DURATION}s"
echo "  Throughput: $((SUCCESS_COUNT / LOAD_DURATION)) req/s"
echo ""
echo "  Backlog ngay sau tai: $BACKLOG_IMMEDIATE events"
echo "  Eventual Consistency Lag: ${CONVERGENCE_LAG}s"
echo "  Chenh lech cuoi cung: $FINAL_DIFF"
echo ""

if [ "$CONVERGED" = true ] && [ "$FINAL_DIFF" -le 0 ]; then
  echo "  [PASS] Kafka consumer tieu thu het backlog"
  echo "  [PASS] So cai ke toan khop toan hoan toan"
  echo "  [PASS] Eventual Consistency Lag = ${CONVERGENCE_LAG}s"
  
  # Tinh consumer throughput
  if [ "$CONVERGENCE_LAG" -gt 0 ]; then
    CONSUMER_THROUGHPUT=$((BACKLOG_IMMEDIATE / CONVERGENCE_LAG))
    echo "  [INFO] Consumer throughput: ~${CONSUMER_THROUGHPUT} events/s"
  fi
else
  echo "  [FAIL] Accounting chua catch up sau ${MAX_WAIT}s"
  echo "  [FAIL] Con $FINAL_DIFF transactions chua duoc hach toan"
fi

# --- Restore fraud limits ---
echo ""
echo "[Cleanup] Restore fraud detection (enabled=true)..."
curl -s -X PUT "http://localhost:8086/api/v1/admin/fraud-config/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"ruleName":"enabled","value":"true"}' > /dev/null
echo "  Done"
echo "============================================================"
