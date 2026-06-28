@echo off
setlocal enabledelayedexpansion

if "%~1"=="" (
    echo Usage: trace-log.bat ^<request-id^>
    echo Example: trace-log.bat a1b2c3d4-e5f6-7890-abcd-ef1234567890
    exit /b 1
)

set REQUEST_ID=%~1
set QUERY=SELECT service_name, http_method, path, status_code, duration_ms, timestamp FROM audit_log WHERE request_id='%REQUEST_ID%' ORDER BY timestamp;

echo.
echo ============================================================
echo   DISTRIBUTED TRACE: %REQUEST_ID%
echo ============================================================
echo.

echo --- user_management_db (port 5432) ---
docker exec user-management-db psql -U postgres -d user_management_db -c "%QUERY%" 2>nul

echo --- wallet_management_db (port 5433) ---
docker exec wallet-management-db psql -U postgres -d wallet_management_db -c "%QUERY%" 2>nul

echo --- transaction_management_db (port 5434) ---
docker exec transaction-management-db psql -U postgres -d transaction_management_db -c "%QUERY%" 2>nul

echo --- payment_management_db (port 5435) ---
docker exec payment-management-db psql -U postgres -d payment_management_db -c "%QUERY%" 2>nul

echo --- accounting_db (port 5436) ---
docker exec accounting-db psql -U postgres -d accounting_db -c "%QUERY%" 2>nul

echo --- fraud_db (port 5437) ---
docker exec fraud-db psql -U postgres -d fraud_db -c "%QUERY%" 2>nul

echo.
echo ============================================================
echo   END TRACE
echo ============================================================
