#!/bin/bash
# ============================================================
# KIEM THU WRITE-BEHIND CACHE DATA DRIFT
# Muc tieu: Chung minh sau dot tai cuc han, write-behind sync
# hoan tat va dua Data Drift (Redis vs PostgreSQL) ve 0.
# ============================================================

API_URL="http://localhost:8080"
WALLET_SERVICE_URL="http://localhost:8082"
TOTAL_REQUESTS=1000
CONCURRENT=100
WALLET_A_IBAN="AL35202111090000000001234567"
TRANSFER_AMOUNT=10
USERNAME="nhanvien"
PASSWORD="johnd@e"
SYNC_INTERVAL=5  # seconds (write-behind cycle)

echo "============================================================"
echo "  KIEM THU WRITE-BEHIND CACHE DATA DRIFT"
echo "============================================================"
echo "  Muc tieu: Drift = 0 sau khi sync cycle hoan tat"
echo "  Write-behind interval: ${SYNC_INTERVAL}s"
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

# --- Buoc 1: Kiem tra drift truoc test (phai = 0 neu he thong on dinh) ---
echo ""
echo "[Buoc 1] Kiem tra drift TRUOC khi bat dau tai..."
DRIFT_BEFORE=$(curl -s "$WALLET_SERVICE_URL/api/v1/wallets/drift-check" \
  -H "Authorization: Bearer $TOKEN")
echo "  Response: $DRIFT_BEFORE"

TOTAL_DRIFT_BEFORE=$(echo "$DRIFT_BEFORE" | python -c "import sys,json; print(json.load(sys.stdin).get('totalDrift',0))" 2>/dev/null)
echo "  Total Drift truoc test: $TOTAL_DRIFT_BEFORE"

# --- Buoc 2: Ban tai cuc han (chi credit, khong can 2 vi) ---
echo ""
echo "[Buoc 2] Ban $TOTAL_REQUESTS requests credit dong thoi ($CONCURRENT threads)..."
echo "  Moi request: credit $TRANSFER_AMOUNT vao vi A"
echo "  Redis se cap nhat ngay, PostgreSQL chua kip sync"
echo ""

# Lay wallet ID tu IBAN
WALLET_A_ID=$(curl -s "$API_URL/api/v1/wallets/iban/$WALLET_A_IBAN" \
  -H "Authorization: Bearer $TOKEN" | python -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null)
echo "  Wallet A ID: $WALLET_A_ID"

START_TIME=$(date +%s)

seq 1 $TOTAL_REQUESTS | xargs -P $CONCURRENT -I {} curl -s -o /dev/null -w "%{http_code}\n" \
  -X POST "$WALLET_SERVICE_URL/api/v1/wallets/$WALLET_A_ID/credit" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"amount\":$TRANSFER_AMOUNT}" \
  > /tmp/writebehind_test_results.txt

END_TIME=$(date +%s)
DURATION_S=$((END_TIME - START_TIME))
if [ "$DURATION_S" -eq 0 ]; then DURATION_S=1; fi

SUCCESS_COUNT=$(grep -c "^204$" /tmp/writebehind_test_results.txt 2>/dev/null || echo "0")
TOTAL_RESPONSES=$(wc -l < /tmp/writebehind_test_results.txt 2>/dev/null || echo "0")

echo ""
echo "  Hoan thanh trong: ${DURATION_S}s"
echo "  Thanh cong (204): $SUCCESS_COUNT / $TOTAL_RESPONSES"
echo "  Throughput: $((TOTAL_REQUESTS / DURATION_S)) req/s"

# --- Buoc 3: Do drift NGAY SAU khi tai ket thuc (truoc sync) ---
echo ""
echo "[Buoc 3] Do drift NGAY SAU tai (truoc khi write-behind sync)..."
DRIFT_DURING=$(curl -s "$WALLET_SERVICE_URL/api/v1/wallets/drift-check" \
  -H "Authorization: Bearer $TOKEN")

TOTAL_DRIFT_DURING=$(echo "$DRIFT_DURING" | python -c "import sys,json; print(json.load(sys.stdin).get('totalDrift',0))" 2>/dev/null)
echo "  Total Drift ngay sau tai: $TOTAL_DRIFT_DURING"
echo "  (Ky vong: > 0, vi Redis da cap nhat nhung PostgreSQL chua sync)"

# Show per-wallet drift
echo "$DRIFT_DURING" | python -c "
import sys,json
data = json.load(sys.stdin)
details = data.get('details', [])
for d in details:
    drift = d.get('drift', 0)
    if float(str(drift)) != 0:
        print(f\"    Wallet {d['walletId']}: Redis={d['redis']} | DB={d['db']} | Drift={drift}\")
" 2>/dev/null

# --- Buoc 4: Doi write-behind sync hoan tat ---
echo ""
echo "[Buoc 4] Doi write-behind sync hoan tat..."
echo "  Doi ${SYNC_INTERVAL}s (1 sync cycle)..."
sleep $SYNC_INTERVAL

# Doi them 2s cho an toan
echo "  Doi them 2s buffer..."
sleep 2

# --- Buoc 5: Do drift SAU KHI sync hoan tat ---
echo ""
echo "[Buoc 5] Do drift SAU KHI write-behind sync hoan tat..."
DRIFT_AFTER=$(curl -s "$WALLET_SERVICE_URL/api/v1/wallets/drift-check" \
  -H "Authorization: Bearer $TOKEN")

TOTAL_DRIFT_AFTER=$(echo "$DRIFT_AFTER" | python -c "import sys,json; print(json.load(sys.stdin).get('totalDrift',0))" 2>/dev/null)
echo "  Total Drift sau sync: $TOTAL_DRIFT_AFTER"

# Show per-wallet drift after sync
echo "$DRIFT_AFTER" | python -c "
import sys,json
data = json.load(sys.stdin)
details = data.get('details', [])
for d in details:
    drift = d.get('drift', 0)
    print(f\"    Wallet {d['walletId']}: Redis={d['redis']} | DB={d['db']} | Drift={drift}\")
" 2>/dev/null

# --- Buoc 6: Ket qua ---
echo ""
echo "============================================================"
echo "  KET QUA KIEM THU WRITE-BEHIND CACHE"
echo "============================================================"
echo "  Load: $TOTAL_REQUESTS requests / $CONCURRENT concurrent"
echo "  Duration: ${DURATION_S}s"
echo "  Throughput: $((TOTAL_REQUESTS / DURATION_S)) req/s"
echo ""
echo "  Drift truoc tai:    $TOTAL_DRIFT_BEFORE"
echo "  Drift ngay sau tai: $TOTAL_DRIFT_DURING (ky vong > 0)"
echo "  Drift sau sync:     $TOTAL_DRIFT_AFTER (ky vong = 0)"
echo ""

# Kiem tra ket qua
PASS=$(python -c "
drift = float('$TOTAL_DRIFT_AFTER')
if drift == 0 or drift == 0.0:
    print('PASS')
else:
    print('FAIL')
" 2>/dev/null)

if [ "$PASS" = "PASS" ]; then
  echo "  [PASS] Write-Behind sync thanh cong"
  echo "  [PASS] Data Drift = 0 sau sync cycle"
  echo "  [PASS] Redis va PostgreSQL nhat quan sau ${SYNC_INTERVAL}s"
else
  echo "  [FAIL] Data Drift != 0 sau sync cycle"
  echo "  [FAIL] Can kiem tra lai write-behind scheduler"
  echo ""
  echo "  Thu doi them 1 sync cycle (${SYNC_INTERVAL}s)..."
  sleep $SYNC_INTERVAL
  DRIFT_RETRY=$(curl -s "$WALLET_SERVICE_URL/api/v1/wallets/drift-check" \
    -H "Authorization: Bearer $TOKEN")
  TOTAL_DRIFT_RETRY=$(echo "$DRIFT_RETRY" | python -c "import sys,json; print(json.load(sys.stdin).get('totalDrift',0))" 2>/dev/null)
  echo "  Drift sau 2 sync cycles: $TOTAL_DRIFT_RETRY"
  if [ "$TOTAL_DRIFT_RETRY" = "0" ] || [ "$TOTAL_DRIFT_RETRY" = "0.0" ]; then
    echo "  [PASS] Drift = 0 sau 2 sync cycles (10s)"
  fi
fi
echo "============================================================"
