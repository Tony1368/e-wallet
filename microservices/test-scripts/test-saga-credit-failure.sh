#!/bin/bash
# ============================================================
# KIEM THU SAGA COMPENSATION - KICH BAN 2
# Credit that bai giua chung (wallet-service bi cham/timeout)
# Muc tieu: Chung minh debit da tru nhung credit fail -> hoan debit
# ============================================================

API_URL="http://localhost:8080"
TOTAL_REQUESTS=200
CONCURRENT=30
WALLET_A_IBAN="AL35202111090000000001234567"
WALLET_B_IBAN="AD1400080001001234567890"
TRANSFER_AMOUNT=50
USERNAME="nhanvien"
PASSWORD="johnd@e"

echo "============================================================"
echo "  KICH BAN 2: CREDIT THAT BAI -> COMPENSATION DEBIT"
echo "============================================================"

# Login
TOKEN=$(curl -s -X POST "$API_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}" | python -c "import sys,json; print(json.load(sys.stdin)['token'])" 2>/dev/null)

if [ -z "$TOKEN" ]; then
  echo "FAIL: Khong the dang nhap"
  exit 1
fi
echo "[Login] Token: ${TOKEN:0:20}..."

# Ghi nhan so du truoc
BALANCE_A_BEFORE=$(curl -s "$API_URL/api/v1/wallets/iban/$WALLET_A_IBAN" \
  -H "Authorization: Bearer $TOKEN" | python -c "import sys,json; d=json.load(sys.stdin); print(d.get('balance',0))" 2>/dev/null)
BALANCE_B_BEFORE=$(curl -s "$API_URL/api/v1/wallets/iban/$WALLET_B_IBAN" \
  -H "Authorization: Bearer $TOKEN" | python -c "import sys,json; d=json.load(sys.stdin); print(d.get('balance',0))" 2>/dev/null)
TOTAL_BEFORE=$(python -c "print(float('$BALANCE_A_BEFORE') + float('$BALANCE_B_BEFORE'))")

echo "[Truoc test] Vi A=$BALANCE_A_BEFORE | Vi B=$BALANCE_B_BEFORE | Tong=$TOTAL_BEFORE"

# Inject fault: them latency 10s vao wallet-service
echo ""
echo "[Inject fault] Them latency 10s vao wallet-service (credit se timeout)..."
docker exec wallet-management-service sh -c "tc qdisc add dev eth0 root netem delay 10000ms" 2>/dev/null || true
sleep 1

# Ban tai
echo "[Load test] Gui $TOTAL_REQUESTS requests ($CONCURRENT concurrent, max-time 5s)..."
START_TIME=$(date +%s)

seq 1 $TOTAL_REQUESTS | xargs -P $CONCURRENT -I {} curl -s -o /dev/null -w "%{http_code}\n" \
  --max-time 5 \
  -X POST "$API_URL/api/v1/payments/transfer" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"fromWalletIban\":\"$WALLET_A_IBAN\",\"toWalletIban\":\"$WALLET_B_IBAN\",\"amount\":$TRANSFER_AMOUNT,\"typeId\":1}" \
  > /tmp/saga_test2_results.txt

END_TIME=$(date +%s)
DURATION_S=$((END_TIME - START_TIME))
if [ "$DURATION_S" -eq 0 ]; then DURATION_S=1; fi

# Go fault injection
echo "[Remove fault] Go latency..."
docker exec wallet-management-service sh -c "tc qdisc del dev eth0 root" 2>/dev/null || true
sleep 5

# Kiem tra so du sau
BALANCE_A_AFTER=$(curl -s "$API_URL/api/v1/wallets/iban/$WALLET_A_IBAN" \
  -H "Authorization: Bearer $TOKEN" | python -c "import sys,json; d=json.load(sys.stdin); print(d.get('balance',0))" 2>/dev/null)
BALANCE_B_AFTER=$(curl -s "$API_URL/api/v1/wallets/iban/$WALLET_B_IBAN" \
  -H "Authorization: Bearer $TOKEN" | python -c "import sys,json; d=json.load(sys.stdin); print(d.get('balance',0))" 2>/dev/null)
TOTAL_AFTER=$(python -c "print(float('$BALANCE_A_AFTER') + float('$BALANCE_B_AFTER'))")

SUCCESS_COUNT=$(grep -c "^201$" /tmp/saga_test2_results.txt 2>/dev/null || echo "0")
FAIL_COUNT=$(grep -cv "^201$" /tmp/saga_test2_results.txt 2>/dev/null || echo "0")

# Phan bo HTTP codes
echo ""
echo "  Phan bo HTTP codes:"
sort /tmp/saga_test2_results.txt | uniq -c | sort -rn | head -10

echo ""
echo "============================================================"
echo "  KET QUA"
echo "============================================================"
echo "  Thoi gian: ${DURATION_S}s"
echo "  Thanh cong (201): $SUCCESS_COUNT | That bai (compensation): $FAIL_COUNT"
echo "  Vi A: $BALANCE_A_BEFORE -> $BALANCE_A_AFTER"
echo "  Vi B: $BALANCE_B_BEFORE -> $BALANCE_B_AFTER"
echo "  Tong truoc: $TOTAL_BEFORE"
echo "  Tong sau:   $TOTAL_AFTER"

DELTA=$(python -c "print(float('$TOTAL_AFTER') - float('$TOTAL_BEFORE'))")
echo "  Delta (ro ri): $DELTA"
echo ""

if [ "$DELTA" = "0.0" ] || [ "$DELTA" = "0" ] || [ "$DELTA" = "-0.0" ]; then
  echo "  [PASS] Compensation hoat dong dung - khong ro ri"
else
  echo "  [FAIL] Ro ri so du = $DELTA"
fi
echo "============================================================"
