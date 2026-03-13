@echo off
REM Build script for E-Wallet Microservices

echo ========================================
echo E-Wallet Microservices Build Script
echo ========================================
echo.

REM Check Java version
echo Checking Java version...
java -version 2>&1 | findstr /C:"version" >nul
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install JDK 21 and set JAVA_HOME
    echo Download from: https://adoptium.net/temurin/releases/?version=21
    pause
    exit /b 1
)

java -version 2>&1 | findstr /C:"21."
if errorlevel 1 (
    echo WARNING: Java 21 is recommended
    echo Current Java version:
    java -version
    echo.
    echo Continue anyway? (Y/N)
    set /p continue=
    if /i not "%continue%"=="Y" exit /b 1
)

echo Java version OK
echo.

REM Check Maven
echo Checking Maven...
where mvn >nul 2>&1
if errorlevel 1 (
    echo Maven not found in PATH, using Maven Wrapper...
    set MVN_CMD=..\backend\mvnw.cmd
) else (
    echo Maven found
    set MVN_CMD=mvn
)
echo.

REM Build Common Service
echo ========================================
echo Building Common Service...
echo ========================================
cd common-service
call %MVN_CMD% clean install
if errorlevel 1 (
    echo ERROR: Failed to build common-service
    cd ..
    pause
    exit /b 1
)
cd ..
echo Common Service built successfully!
echo.

REM Build All Services
echo ========================================
echo Building All Services...
echo ========================================
call %MVN_CMD% clean install -DskipTests
if errorlevel 1 (
    echo ERROR: Failed to build services
    pause
    exit /b 1
)
echo All services built successfully!
echo.

echo ========================================
echo BUILD SUCCESSFUL!
echo ========================================
echo.
echo All services have been built successfully.
echo.
echo Next steps:
echo 1. Create databases: create-databases.bat
echo 2. Run services: docker-compose up
echo    OR run individually with: java -jar [service]/target/[service].jar
echo.

pause
