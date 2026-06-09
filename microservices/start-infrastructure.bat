@echo off
echo ===================================================
echo   E-Wallet Infrastructure Startup Script
echo   Redis + Kafka + PostgreSQL
echo ===================================================
echo.

cd /d "%~dp0"

echo [1/4] Khoi dong Infrastructure (Redis, Zookeeper, Kafka, Databases)...
docker compose up -d redis zookeeper user-db wallet-db transaction-db payment-db accounting-db fraud-db
echo.

echo [2/4] Doi Redis san sang...
:wait_redis
docker exec ewallet-redis redis-cli ping >nul 2>&1
if %errorlevel% neq 0 (
    timeout /t 2 /nobreak >nul
    goto wait_redis
)
echo       Redis: OK

echo [2/4] Doi Zookeeper san sang...
:wait_zk
docker exec ewallet-zookeeper bash -c "echo ruok | nc localhost 2181" >nul 2>&1
if %errorlevel% neq 0 (
    timeout /t 2 /nobreak >nul
    goto wait_zk
)
echo       Zookeeper: OK

echo [3/4] Khoi dong Kafka (can Zookeeper truoc)...
docker compose up -d kafka
echo       Doi Kafka san sang (co the mat 30s)...

:wait_kafka
docker exec ewallet-kafka kafka-broker-api-versions --bootstrap-server localhost:9092 >nul 2>&1
if %errorlevel% neq 0 (
    timeout /t 3 /nobreak >nul
    goto wait_kafka
)
echo       Kafka: OK
echo.

echo [4/4] Kiem tra tat ca infrastructure...
echo.
echo --- Redis ---
docker exec ewallet-redis redis-cli ping
echo.
echo --- Kafka Topics ---
docker exec ewallet-kafka kafka-topics --list --bootstrap-server localhost:9092
echo.
echo --- Databases ---
docker exec user-management-db pg_isready -U postgres
docker exec wallet-management-db pg_isready -U postgres
docker exec transaction-management-db pg_isready -U postgres
docker exec payment-management-db pg_isready -U postgres
docker exec accounting-db pg_isready -U postgres
docker exec fraud-db pg_isready -U postgres
echo.

echo ===================================================
echo   Infrastructure READY!
echo   Redis:     localhost:6379
echo   Kafka:     localhost:9092
echo   Zookeeper: localhost:2181
echo ===================================================
echo.
echo Tiep theo, chay: docker compose up -d --build
echo De khoi dong tat ca microservices.
echo.
pause
