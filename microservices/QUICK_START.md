# 🚀 Quick Start Guide

## Yêu cầu hệ thống

- ✅ **JDK 21** (LTS) - [Download here](https://adoptium.net/temurin/releases/?version=21)
- ✅ **Maven 3.9+** hoặc sử dụng Maven Wrapper (đã có sẵn)
- ✅ **PostgreSQL 16** - [Download here](https://www.postgresql.org/download/)
- ✅ **Node.js 18+** (cho frontend) - [Download here](https://nodejs.org/)
- ⚠️ **Docker Desktop** (optional, cho deployment) - [Download here](https://www.docker.com/products/docker-desktop/)

## ⚡ Bắt đầu nhanh (5 phút)

### Bước 1: Kiểm tra Java

```bash
# Windows
java -version

# Nên thấy: openjdk version "21.x.x"
```

**Nếu chưa có JDK 21:**
1. Download từ: https://adoptium.net/temurin/releases/?version=21
2. Cài đặt
3. Set JAVA_HOME:
   ```bash
   # Windows (Run as Administrator)
   setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-21.x.x" /M
   setx PATH "%PATH%;%JAVA_HOME%\bin" /M
   ```
4. Restart terminal và kiểm tra lại: `java -version`

### Bước 2: Build Common Service

```bash
cd microservices\common-service

# Option 1: Nếu có Maven
mvn clean install

# Option 2: Sử dụng Maven Wrapper
..\..\backend\mvnw.cmd clean install

# Option 3: Sử dụng build script
cd ..
build.bat
```

**Kết quả mong đợi:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: X.XXX s
```

### Bước 3: Tạo Databases

```bash
cd microservices

# Windows
create-databases.bat

# Linux/Mac
chmod +x create-databases.sh
./create-databases.sh
```

**Lưu ý:** Đảm bảo PostgreSQL đang chạy và có user `postgres` với password `postgres`

### Bước 4: Build tất cả services

```bash
# Windows
build.bat

# Hoặc manual
cd common-service && mvn clean install && cd ..
cd user-management-service && mvn clean package && cd ..
cd api-gateway && mvn clean package && cd ..
```

### Bước 5: Run Services

**Option A: Docker Compose (Recommended)**
```bash
cd microservices
docker-compose up --build
```

**Option B: Run từng service riêng (Development)**
```bash
# Terminal 1 - User Service
cd user-management-service
java -jar target/user-management-service-1.0.0.jar

# Terminal 2 - API Gateway
cd api-gateway
java -jar target/api-gateway-1.0.0.jar
```

### Bước 6: Update Frontend

```bash
cd frontend\src\services

# Edit axios.js
# Change baseURL to: "http://localhost:8080/api/v1"
```

**axios.js:**
```javascript
import axios from "axios";

const instance = axios.create({ 
    baseURL: "http://localhost:8080/api/v1"  // API Gateway
});

// ... rest of the code
```

### Bước 7: Run Frontend

```bash
cd frontend
npm install
npm start
```

### Bước 8: Test

**Health Checks:**
```bash
# User Service
curl http://localhost:8081/actuator/health

# API Gateway
curl http://localhost:8080/actuator/health
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

**Access Application:**
- Frontend: http://localhost:3000
- API Gateway: http://localhost:8080
- User Service Swagger: http://localhost:8081/swagger-ui.html

## 🔧 Troubleshooting

### Lỗi: "JAVA_HOME not found"

**Giải pháp:**
```bash
# Windows (Run as Administrator)
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-21.x.x" /M
setx PATH "%PATH%;%JAVA_HOME%\bin" /M

# Restart terminal
```

### Lỗi: "Fatal error compiling"

**Giải pháp:**
```bash
# Verify Java version
java -version

# Should show: openjdk version "21.x.x"
# If not, install JDK 21

# Clean and rebuild
mvn clean install -U
```

### Lỗi: "Connection refused" khi connect database

**Giải pháp:**
1. Kiểm tra PostgreSQL đang chạy:
   ```bash
   # Windows
   services.msc
   # Tìm "postgresql" và start
   ```

2. Kiểm tra credentials trong `application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/user_management_db
       username: postgres
       password: postgres
   ```

3. Test connection:
   ```bash
   psql -U postgres -h localhost
   ```

### Lỗi: "Port already in use"

**Giải pháp:**
```bash
# Windows - Kill process on port
netstat -ano | findstr :8080
taskkill /PID [PID] /F

# Change port in application.yml
server:
  port: 8081  # Change to available port
```

### Lỗi: Build failed với Lombok

**Giải pháp:**
```bash
# Verify JDK 21
java -version

# Clean Maven cache
rmdir /s /q %USERPROFILE%\.m2\repository

# Rebuild
mvn clean install -U
```

## 📚 Default Credentials

### Database
- **Host:** localhost
- **Port:** 5432 (user), 5433 (wallet), 5434 (transaction), 5435 (payment)
- **Username:** postgres
- **Password:** postgres

### Application Users
- **Admin:**
  - Username: `admin`
  - Password: `admin123`
  
- **User:**
  - Username: `thailq`
  - Password: `user123`

## 🎯 Service Ports

| Service | Port | URL |
|---------|------|-----|
| Frontend | 3000 | http://localhost:3000 |
| API Gateway | 8080 | http://localhost:8080 |
| User Service | 8081 | http://localhost:8081 |
| Wallet Service | 8082 | http://localhost:8082 |
| Transaction Service | 8083 | http://localhost:8083 |
| Payment Service | 8084 | http://localhost:8084 |

## 📖 Documentation

- **README.md** - Kiến trúc tổng quan
- **IMPLEMENTATION_GUIDE.md** - Hướng dẫn implementation chi tiết
- **MIGRATION_SUMMARY.md** - Tổng kết migration
- **JAVA_VERSION_CHANGE.md** - Giải thích về Java version

## 🆘 Cần trợ giúp?

1. Đọc **IMPLEMENTATION_GUIDE.md** cho hướng dẫn chi tiết
2. Kiểm tra logs: `logs/` folder
3. Check Swagger UI: http://localhost:8081/swagger-ui.html
4. Verify health: http://localhost:8080/actuator/health

## ✅ Checklist

- [ ] JDK 21 installed và JAVA_HOME set
- [ ] PostgreSQL installed và running
- [ ] Databases created (user_management_db, etc.)
- [ ] Common service built successfully
- [ ] User service built successfully
- [ ] API Gateway built successfully
- [ ] Frontend baseURL updated
- [ ] All services running
- [ ] Can login to application
- [ ] Can access Swagger UI

## 🎉 Success!

Nếu tất cả các bước trên hoàn thành, bạn đã successfully migrate từ Monolithic sang Microservices!

Access application tại: **http://localhost:3000**

---

**Next Steps:**
- Hoàn thiện Wallet Management Service
- Hoàn thiện Transaction Management Service
- Hoàn thiện Payment Management Service
- Implement inter-service communication
- Add monitoring và logging
- Deploy to production
