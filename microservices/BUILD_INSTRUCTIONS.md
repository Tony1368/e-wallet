# 🔨 Build Instructions

## ✅ Prerequisites

1. **JDK 21** installed and JAVA_HOME set
2. **Maven 3.9+** (or use Maven Wrapper)
3. **PostgreSQL 16** running

## 🚀 Quick Build

### Option 1: Using build script (Recommended)

```bash
cd microservices
build.bat
```

### Option 2: Manual build

```bash
cd microservices

# Build common service first
cd common-service
mvn clean install
cd ..

# Build all services
mvn clean install -DskipTests
```

### Option 3: Using Maven Wrapper

```bash
cd microservices
..\backend\mvnw.cmd clean install -DskipTests
```

## 📦 Build Output

After successful build, you'll find JAR files in:

```
common-service/target/common-service-1.0.0.jar
user-management-service/target/user-management-service-1.0.0.jar
wallet-management-service/target/wallet-management-service-1.0.0.jar
transaction-management-service/target/transaction-management-service-1.0.0.jar
payment-management-service/target/payment-management-service-1.0.0.jar
api-gateway/target/api-gateway-1.0.0.jar
```

## ⚠️ Common Issues

### Error: "Child module does not exist"

**Solution:** All pom.xml files have been created. Just run `mvn clean install`

### Error: "JAVA_HOME not found"

**Solution:**
```bash
# Windows (Run as Administrator)
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-21.x.x" /M
setx PATH "%PATH%;%JAVA_HOME%\bin" /M
```

### Error: "Fatal error compiling"

**Solution:** Verify Java 21 is installed
```bash
java -version
# Should show: openjdk version "21.x.x"
```

## ✨ Success Indicators

You should see:
```
[INFO] Reactor Summary:
[INFO] 
[INFO] e-wallet-microservices ......................... SUCCESS
[INFO] common-service ................................. SUCCESS
[INFO] user-management-service ........................ SUCCESS
[INFO] wallet-management-service ...................... SUCCESS
[INFO] transaction-management-service ................. SUCCESS
[INFO] payment-management-service ..................... SUCCESS
[INFO] api-gateway .................................... SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

## 🎯 Next Steps

1. ✅ Build completed
2. Create databases: `create-databases.bat`
3. Run services: See QUICK_START.md
4. Implement business logic: See IMPLEMENTATION_GUIDE.md

## 📚 More Information

- **QUICK_START.md** - Complete setup guide
- **IMPLEMENTATION_GUIDE.md** - Detailed implementation steps
- **README.md** - Architecture overview
