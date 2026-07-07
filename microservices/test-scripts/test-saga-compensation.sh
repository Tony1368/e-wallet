#!/bin/bash
# ============================================================
# KIỂM THỬ SAGA COMPENSATION DƯỚI TẢI CAO
# Mục tiêu: Chứng minh khi xảy ra lỗi hàng loạt, cơ chế bù trừ
# Saga giải phóng số dư Redis kịp thời, không rò rỉ tài chính.
# ============================================================

API_URL="http://localhost:8080"
TOTAL_REQUESTS=500
CONCURRENT=50
WALLET_A_IBAN="AL35202111090000000001234567"
WALLET_B_IBAN="AD1400080001001234567890"
TRANSFER_AMOUNT=100
USERNAME="nhanvien"
PASSWORD="johnd@e"

echo "============================================================"
echo "  KIỂM THỬ SAGA COMPENSATION DƯỚI TẢI CAO"
echo "============================================================"
echo ""

# --- Bước 0: Login lấy token ---
echo "[Bước 0] Đăng nhập lấy JWT token..."
LOGIN_RESPONSE=$(curl -s -X POST "$API_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

TOKEN=$(echo "$LOGIN_RESPONSE" | python -c "import sys,json; print(json.load(sys.stdin)['token'])" 2>/dev/null)

if [ -z "$TOKEN" ]; then
  echo "FAIL: Không thể đăng nhập. Response: $LOGIN_RESPONSE"
  exit 1
fi
echo "  Token: ${TOKEN:0:20}..."

# --- Bước 1: Ghi nhận số dư ban đầu ---
echo ""
echo "[Bước 1] Ghi nhận số dư ban đầu trên Redis..."
BALANCE_A_BEFORE=$(curl -s "$API_URL/api/v1/wallets/iban/$WALLET_A_IBAN" \
  -H "Authorization: Bearer $TOKEN" | python -c "import sys,json; d=json.load(sys.stdin); print(d.get('balance',0))" 2>/dev/null)
BALANCE_B_BEFORE=$(curl -s "$API_URL/api/v1/wallets/iban/$WALLET_B_IBAN" \
  -H "Authorization: Bearer $TOKEN" | python -c "import sys,json; d=json.load(sys.stdin); print(d.get('balance',0))" 2>/dev/null)

TOTAL_BEFORE=$(python -c "print(float('$BALANCE_A_BEFORE') + float('$BALANCE_B_BEFORE'))")

echo "  Ví A ($WALLET_A_IBAN): $BALANCE_A_BEFORE"
echo "  Ví B ($WALLET_B_IBAN): $BALANCE_B_BEFORE"
echo "  TỔNG SỐ DƯ HỆ THỐNG: $TOTAL_BEFORE"

# Kiểm tra balance hợp lệ
VALID=$(python -c "
a=float('$BALANCE_A_BEFORE')
if a < 0:
    print('INVALID')
else:
    print('OK')
")
if [ "$VALID" = "INVALID" ]; then
  echo ""
  echo "  ⚠️  CẢNH BÁO: Ví A đang âm ($BALANCE_A_BEFORE). Cần reset balance trước khi test."
  echo "  Hãy nạp tiền cho ví A trước rồi chạy lại script."
  exit 1
fi

# --- Bước 2: Ngắt Transaction Service để buộc compensation ---
echo ""
echo "[Bước 2] Ngắt Transaction Service (buộc Step 4 thất bại → trigger compensation)..."
docker pause transaction-management-service
sleep 2
echo "  Transaction Service đã bị PAUSE"

# --- Bước 3: Bắn tải transfer đồng thời ---
echo ""
echo "[Bước 3] Gửi $TOTAL_REQUESTS yêu cầu transfer đồng thời ($CONCURRENT threads)..."
echo "  Mỗi request: chuyển $TRANSFER_AMOUNT từ Ví A → Ví B"
echo "  Kỳ vọng: TẤT CẢ đều thất bại + compensation hoàn tiền"
echo ""

START_TIME=$(date +%s)

# Sử dụng xargs để gửi đồng thời
seq 1 $TOTAL_REQUESTS | xargs -P $CONCURRENT -I {} curl -s -o /dev/null -w "%{http_code}\n" \
  -X POST "$API_URL/api/v1/payments/transfer" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"fromWalletIban\":\"$WALLET_A_IBAN\",\"toWalletIban\":\"$WALLET_B_IBAN\",\"amount\":$TRANSFER_AMOUNT,\"typeId\":1,\"description\":\"Saga compensation test #{}\"}" \
  > /tmp/saga_test_results.txt

END_TIME=$(date +%s)
DURATION_S=$((END_TIME - START_TIME))
if [ "$DURATION_S" -eq 0 ]; then DURATION_S=1; fi

# Đếm kết quả
SUCCESS_COUNT=$(grep -c "^201$" /tmp/saga_test_results.txt 2>/dev/null || echo "0")
FAIL_COUNT=$(grep -cv "^201$" /tmp/saga_test_results.txt 2>/dev/null || echo "0")
TOTAL_RESPONSES=$(wc -l < /tmp/saga_test_results.txt 2>/dev/null || echo "0")

# Hiển thị phân bố HTTP codes
echo "  Phân bố HTTP codes:"
sort /tmp/saga_test_results.txt | uniq -c | sort -rn | head -10

echo ""
echo "  Hoàn thành trong: ${DURATION_S}s"
echo "  Thanh cong (201): $SUCCESS_COUNT"
echo "  That bai (compensation triggered): $FAIL_COUNT"
echo "  Tong responses: $TOTAL_RESPONSES"

# --- Bước 4: Bật lại Transaction Service ---
echo ""
echo "[Bước 4] Bật lại Transaction Service..."
docker unpause transaction-management-service
sleep 3
echo "  Transaction Service đã UNPAUSE"

# --- Bước 5: Kiểm tra số dư sau test ---
echo ""
echo "[Bước 5] Kiểm tra số dư sau khi compensation hoàn tất..."
BALANCE_A_AFTER=$(curl -s "$API_URL/api/v1/wallets/iban/$WALLET_A_IBAN" \
  -H "Authorization: Bearer $TOKEN" | python -c "import sys,json; d=json.load(sys.stdin); print(d.get('balance',0))" 2>/dev/null)
BALANCE_B_AFTER=$(curl -s "$API_URL/api/v1/wallets/iban/$WALLET_B_IBAN" \
  -H "Authorization: Bearer $TOKEN" | python -c "import sys,json; d=json.load(sys.stdin); print(d.get('balance',0))" 2>/dev/null)

TOTAL_AFTER=$(python -c "print(float('$BALANCE_A_AFTER') + float('$BALANCE_B_AFTER'))")

echo "  Ví A: $BALANCE_A_BEFORE → $BALANCE_A_AFTER"
echo "  Ví B: $BALANCE_B_BEFORE → $BALANCE_B_AFTER"
echo "  TỔNG SỐ DƯ HỆ THỐNG: $TOTAL_BEFORE → $TOTAL_AFTER"

# --- Bước 6: Đánh giá kết quả ---
echo ""
echo "============================================================"
echo "  KẾT QUẢ KIỂM THỬ"
echo "============================================================"

export PYTHONIOENCODING=utf-8
DELTA=$(python -c "print(float('$TOTAL_AFTER') - float('$TOTAL_BEFORE'))")
THROUGHPUT=$((TOTAL_REQUESTS / DURATION_S))

echo "  Delta (ro ri so du): $DELTA"
echo ""

if [ "$DELTA" = "0.0" ] || [ "$DELTA" = "0" ] || [ "$DELTA" = "-0.0" ]; then
  echo "  [PASS] Khong co ro ri so du tai chinh"
  echo "  [PASS] Saga Compensation hoat dong dung duoi tai $CONCURRENT concurrent"
  echo "  [PASS] Tong so du he thong bao toan (invariant giu vung)"
else
  echo "  [FAIL] Phat hien ro ri so du = $DELTA"
  echo "  [FAIL] Saga Compensation KHONG hoan tra day du"
fi

echo ""
echo "  Throughput: $THROUGHPUT req/s"
if [ "$FAIL_COUNT" -gt 0 ]; then
  COMP_RATE=$(python -c "print(f'{$FAIL_COUNT * 100.0 / $TOTAL_RESPONSES:.1f}')")
  echo "  Compensation rate: ${COMP_RATE}%"
fi
echo "  Duration: ${DURATION_S}s"
echo "============================================================"
