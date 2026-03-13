# 🐳 Docker Deployment Guide

## 🎯 2 Options để chạy trên Docker

### Option 1: Monolithic Backend (⚡ Nhanh nhất - 5 phút)

Chạy backend monolithic đã có sẵn business logic.

**Ưu điểm:**
- ✅ Chạy ngay, không cần implement gì thêm
- ✅ Đầy đủ chức năng
- ✅ Frontend hoạt động 100%

**Nhược điểm:**
- ❌ Không phải microservices architecture
- ❌ Chỉ để test, không phải final solution

**Cách chạy:**

```bash
# Từ thư mục root (e-wallet)
docker-compose -f docker-compose-monolithic.yml up --build
```

**Services:**
- PostgreSQL: localhost:5432
- Backend: localhost:8082
- Frontend: localhost:3000

**Test:**
- Access: http://localhost:3000
- Login: admin / admin123

---

### Option 2: Microservices (🔧 Cần implement - 2-3 giờ)

Chạy microservices architecture với User Service.

**Ưu điểm:**
- ✅ Đúng microservices architecture
- ✅ Có thể scale từng service
- ✅ Final solution

**Nhược điểm:**
- ❌ Cần implement User Service trước
- ❌ Mất thời gian hơn

**Prerequisites:**

1. **Implement User Service** (bắt buộc):
   ```bash
   cd microservices
   copy-user-service-files.bat
   # Fix package names, User.java, etc.
   # See NEXT_STEPS.md for details
   ```

2. **Build User Service:**
   ```bash
   cd microservices\user-management-service
   mvn clean package
   ```

**Cách chạy:**

```bash
# Từ thư mục root (e-wallet)
docker-compose -f docker-compose-microservices-minimal.yml up --build
```

**Services:**
- User DB: localhost:5432
- User Service: localhost:8081
- API Gateway: localhost:8080
- Frontend: localhost:3000

**Test:**
- Access: http://localhost:3000
- Login: admin / admin123

---

## 🚀 Quick Start - Option 1 (Recommended for Testing)

### Step 1: Verify Docker is running

```bash
docker --version
docker-compose --version
```

### Step 2: Build and Run

```bash
cd d:\SourceCode\e-wallet
docker-compose -f docker-compose-monolithic.yml up --build
```

**First time build:** ~5-10 minutes (downloading images, building)

**Subsequent runs:** ~1-2 minutes

### Step 3: Wait for services to start

Watch the logs:
```
postgres_1  | database system is ready to accept connections
backend_1   | Started EWalletApplication in X.XXX seconds
frontend_1  | webpack compiled successfully
```

### Step 4: Access Application

Open browser: http://localhost:3000

**Login:**
- Username: `admin`
- Password: `admin123`

### Step 5: Stop services

```bash
# Press Ctrl+C in terminal
# Or in new terminal:
docker-compose -f docker-compose-monolithic.yml down
```

---

## 🔧 Option 2 - Microservices (Step by Step)

### Prerequisites

1. **Implement User Service:**

Follow `NEXT_STEPS.md`:
- Copy files: `copy-user-service-files.bat`
- Fix package names
- Fix User.java
- Copy JwtUtils.java
- Update UserManagementApplication.java

2. **Build User Service:**

```bash
cd microservices\user-management-service
mvn clean package
```

Verify JAR exists:
```bash
dir target\user-management-service-1.0.0.jar
```

### Step 1: Build and Run

```bash
cd d:\SourceCode\e-wallet
docker-compose -f docker-compose-microservices-minimal.yml up --build
```

### Step 2: Verify Services

**User Service:**
```bash
curl http://localhost:8081/actuator/health
```

**API Gateway:**
```bash
curl http://localhost:8080/actuator/health
```

**Test Login:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

### Step 3: Access Application

http://localhost:3000

---

## 📊 Architecture Comparison

### Monolithic (Option 1)
```
Frontend (3000) → Backend (8082) → PostgreSQL (5432)
```

### Microservices (Option 2)
```
Frontend (3000) → API Gateway (8080) → User Service (8081) → User DB (5432)
```

---

## 🐛 Troubleshooting

### Error: "port is already allocated"

**Solution:**
```bash
# Stop all containers
docker-compose down

# Check what's using the port
netstat -ano | findstr :8082
netstat -ano | findstr :5432

# Kill the process or change port in docker-compose.yml
```

### Error: "Cannot connect to Docker daemon"

**Solution:**
1. Start Docker Desktop
2. Wait for Docker to fully start
3. Try again

### Error: Build fails in Docker

**Solution:**
```bash
# Clean Docker cache
docker system prune -a

# Rebuild
docker-compose up --build --force-recreate
```

### Error: Database connection refused

**Solution:**
```bash
# Check database is healthy
docker-compose ps

# Check logs
docker-compose logs postgres

# Restart services
docker-compose restart
```

### Frontend can't connect to backend

**Solution:**

Check `frontend/src/services/axios.js`:

**For Monolithic:**
```javascript
baseURL: "http://localhost:8082/api/v1"
```

**For Microservices:**
```javascript
baseURL: "http://localhost:8080/api/v1"
```

---

## 🔄 Development Workflow

### Option 1: Monolithic

```bash
# Start
docker-compose -f docker-compose-monolithic.yml up

# Make changes to backend code
# Rebuild only backend
docker-compose -f docker-compose-monolithic.yml up --build backend

# Stop
docker-compose -f docker-compose-monolithic.yml down
```

### Option 2: Microservices

```bash
# Start
docker-compose -f docker-compose-microservices-minimal.yml up

# Make changes to User Service
# Rebuild only User Service
docker-compose -f docker-compose-microservices-minimal.yml up --build user-service

# Stop
docker-compose -f docker-compose-microservices-minimal.yml down
```

---

## 📦 Docker Commands Cheat Sheet

```bash
# Build and start
docker-compose up --build

# Start in background
docker-compose up -d

# Stop
docker-compose down

# Stop and remove volumes
docker-compose down -v

# View logs
docker-compose logs

# Follow logs
docker-compose logs -f

# View logs for specific service
docker-compose logs backend

# List running containers
docker-compose ps

# Restart service
docker-compose restart backend

# Execute command in container
docker-compose exec backend bash

# Remove all containers and images
docker system prune -a
```

---

## 🎯 Recommendations

### For Testing/Demo:
✅ **Use Option 1 (Monolithic)**
- Fastest to get running
- Full functionality
- No implementation needed

### For Learning/Production:
✅ **Use Option 2 (Microservices)**
- Proper architecture
- Scalable
- Production-ready

### Migration Path:
1. Start with Option 1 to verify everything works
2. Implement User Service
3. Switch to Option 2
4. Gradually implement other services
5. Eventually use full `docker-compose.yml` with all services

---

## 📚 Files Created

- ✅ `docker-compose-monolithic.yml` - Monolithic deployment
- ✅ `docker-compose-microservices-minimal.yml` - Minimal microservices
- ✅ `backend/src/main/resources/application-docker.yml` - Backend Docker config
- ✅ `microservices/user-management-service/Dockerfile` - User Service Dockerfile
- ✅ `microservices/user-management-service/src/main/resources/application-docker.yml` - User Service Docker config
- ✅ `microservices/api-gateway/src/main/resources/application-docker.yml` - Gateway Docker config

---

## ✅ Quick Checklist

**Option 1 (Monolithic):**
- [ ] Docker Desktop running
- [ ] Run: `docker-compose -f docker-compose-monolithic.yml up --build`
- [ ] Wait for services to start
- [ ] Access: http://localhost:3000
- [ ] Login: admin / admin123

**Option 2 (Microservices):**
- [ ] Docker Desktop running
- [ ] User Service implemented (see NEXT_STEPS.md)
- [ ] User Service built: `mvn clean package`
- [ ] Run: `docker-compose -f docker-compose-microservices-minimal.yml up --build`
- [ ] Wait for services to start
- [ ] Access: http://localhost:3000
- [ ] Login: admin / admin123

---

## 🎉 Success Indicators

**Monolithic:**
```
✅ postgres_1  | ready to accept connections
✅ backend_1   | Started EWalletApplication
✅ frontend_1  | webpack compiled successfully
```

**Microservices:**
```
✅ user-db_1      | ready to accept connections
✅ user-service_1 | Started UserManagementApplication
✅ api-gateway_1  | Started ApiGatewayApplication
✅ frontend_1     | webpack compiled successfully
```

---

**Recommendation:** Start with **Option 1 (Monolithic)** để test ngay, sau đó chuyển sang **Option 2 (Microservices)** khi đã implement User Service.
