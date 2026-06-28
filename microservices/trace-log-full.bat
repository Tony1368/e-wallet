@echo off
setlocal enabledelayedexpansion

if "%~1"=="" (
    echo Usage: trace-log-full.bat ^<request-id^>
    exit /b 1
)

set REQUEST_ID=%~1
set QUERY=SELECT id, service_name, http_method, path, status_code, duration_ms, username, client_ip, caller_service, level, request_body, response_body, timestamp FROM audit_log WHERE request_id='%REQUEST_ID%' ORDER BY timestamp;

echo.
echo ============================================================
echo   FULL TRACE: %REQUEST_ID%
echo ============================================================

for %%D in (
    "user-management-db:user_management_db"
    "wallet-management-db:wallet_management_db"
    "transaction-management-db:transaction_management_db"
    "payment-management-db:payment_management_db"
    "accounting-db:accounting_db"
    "fraud-db:fraud_db"
) do (
    for /f "tokens=1,2 delims=:" %%A in (%%D) do (
        echo.
        echo --- %%B ---
        docker exec %%A psql -U postgres -d %%B -x -c "%QUERY%" 2>nul
    )
)

echo.
echo ============================================================
