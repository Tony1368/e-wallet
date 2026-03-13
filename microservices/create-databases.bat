@echo off
REM Script to create all microservices databases on Windows

echo Creating microservices databases...

SET DB_USER=postgres
SET DB_PASSWORD=postgres
SET DB_HOST=localhost
SET DB_PORT=5432
SET PGPASSWORD=%DB_PASSWORD%

psql -U %DB_USER% -h %DB_HOST% -p %DB_PORT% -c "CREATE DATABASE user_management_db;"
psql -U %DB_USER% -h %DB_HOST% -p %DB_PORT% -c "CREATE DATABASE wallet_management_db;"
psql -U %DB_USER% -h %DB_HOST% -p %DB_PORT% -c "CREATE DATABASE transaction_management_db;"
psql -U %DB_USER% -h %DB_HOST% -p %DB_PORT% -c "CREATE DATABASE payment_management_db;"

echo.
echo All databases created successfully!
echo.
echo Databases:
echo   - user_management_db
echo   - wallet_management_db
echo   - transaction_management_db
echo   - payment_management_db

pause
