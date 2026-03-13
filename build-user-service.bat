@echo off
echo ========================================
echo Building User Management Service
echo ========================================
echo.

cd microservices\user-management-service

echo Checking for Maven...
where mvn >nul 2>&1
if errorlevel 1 (
    echo Maven not found in PATH
    echo Please install Maven or set JAVA_HOME and try again
    pause
    exit /b 1
)

echo Building...
call mvn clean package -DskipTests

if errorlevel 1 (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    pause
    exit /b 1
)

echo.
echo ========================================
echo BUILD SUCCESS!
echo ========================================
echo.
echo JAR file created at:
echo microservices\user-management-service\target\user-management-service-1.0.0.jar
echo.
echo Next steps:
echo 1. Run: cd microservices
echo 2. Run: docker-compose -f ..\docker-compose-microservices-minimal.yml up --build
echo.

pause
