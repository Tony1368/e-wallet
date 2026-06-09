@echo off
echo ===================================================
echo   Kiem tra Redis va Kafka
echo ===================================================
echo.

echo === REDIS ===
echo [Test 1] Ping:
docker exec ewallet-redis redis-cli ping
echo.

echo [Test 2] Set/Get:
docker exec ewallet-redis redis-cli SET e-wallet:test "working"
docker exec ewallet-redis redis-cli GET e-wallet:test
docker exec ewallet-redis redis-cli DEL e-wallet:test
echo.

echo [Test 3] Memory Info:
docker exec ewallet-redis redis-cli INFO memory | findstr used_memory_human
echo.

echo [Test 4] Wallet Balance Keys:
docker exec ewallet-redis redis-cli KEYS "wallet:balance:*"
echo.

echo === KAFKA ===
echo [Test 1] Broker Versions:
docker exec ewallet-kafka kafka-broker-api-versions --bootstrap-server localhost:9092 >nul 2>&1
if %errorlevel% equ 0 (
    echo Kafka broker: CONNECTED
) else (
    echo Kafka broker: FAILED
)
echo.

echo [Test 2] Topics:
docker exec ewallet-kafka kafka-topics --list --bootstrap-server localhost:9092
echo.

echo [Test 3] Consumer Groups:
docker exec ewallet-kafka kafka-consumer-groups --list --bootstrap-server localhost:9092
echo.

echo [Test 4] Topic Detail (transaction-events):
docker exec ewallet-kafka kafka-topics --describe --bootstrap-server localhost:9092 --topic transaction-events 2>nul
if %errorlevel% neq 0 (
    echo       Topic chua duoc tao. Se tu dong tao khi co giao dich dau tien.
)
echo.

echo === SERVICES HEALTH ===
echo API Gateway:
curl -s http://localhost:8080/actuator/health 2>nul | findstr status
echo.
echo Wallet Service:
curl -s http://localhost:8082/actuator/health 2>nul | findstr status
echo.
echo Transaction Service:
curl -s http://localhost:8083/actuator/health 2>nul | findstr status
echo.
echo Accounting Service:
curl -s http://localhost:8085/actuator/health 2>nul | findstr status
echo.

echo ===================================================
echo   Kiem tra hoan tat!
echo ===================================================
pause
