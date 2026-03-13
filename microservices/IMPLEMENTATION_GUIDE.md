# Hướng dẫn Implementation Chi tiết

## 📋 Tổng quan

File này hướng dẫn chi tiết cách hoàn thiện các microservices còn lại dựa trên code monolithic hiện tại.

## 🔧 Bước 1: Setup Common Service

Common Service đã được tạo với:
- ✅ Constants.java
- ✅ MessageKeys.java
- ✅ Exception classes
- ✅ IbanValidator
- ✅ CommandResponse DTO

### Build Common Service

```bash
cd microservices/common-service
mvn clean install
```

## 👤 Bước 2: Hoàn thiện User Management Service

### 2.1 Copy Entities

Copy từ `backend/src/main/java/com/hust/thailq/domain/entity/`:
- User.java → `user-management-service/src/main/java/com/hust/thailq/user/domain/entity/`
- Role.java → `user-management-service/src/main/java/com/hust/thailq/user/domain/entity/`
- UserSession.java → `user-management-service/src/main/java/com/hust/thailq/user/domain/entity/`
- UserActivity.java → `user-management-service/src/main/java/com/hust/thailq/user/domain/entity/`

**Quan trọng**: Remove relationship với Wallet:
```java
// REMOVE THIS from User.java
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
private Set<Wallet> wallets = new HashSet<>();
```

### 2.2 Copy Enums

Copy từ `backend/src/main/java/com/hust/thailq/domain/enums/`:
- RoleType.java → `user-management-service/src/main/java/com/hust/thailq/user/domain/enums/`

### 2.3 Copy DTOs

Copy từ `backend/src/main/java/com/hust/thailq/dto/`:

**Request DTOs**:
- LoginRequest.java
- SignupRequest.java

**Response DTOs**:
- JwtResponse.java
- UserResponse.java
- RoleResponse.java
- UserSessionResponse.java
- UserActivityResponse.java

**Mappers**:
- SignupRequestMapper.java
- UserResponseMapper.java
- UserSessionResponseMapper.java
- UserActivityResponseMapper.java

### 2.4 Copy Repositories

Copy từ `backend/src/main/java/com/hust/thailq/repository/`:
- UserRepository.java
- RoleRepository.java
- UserSessionRepository.java
- UserActivityRepository.java

### 2.5 Copy Security

Copy từ `backend/src/main/java/com/hust/thailq/security/`:
- JwtUtils.java (đã update cho jjwt 0.12.6)
- AuthTokenFilter.java
- AuthEntryPointJwt.java
- UserDetailsImpl.java
- UserDetailsServiceImpl.java

### 2.6 Copy Services

Copy từ `backend/src/main/java/com/hust/thailq/service/`:
- AuthService.java
- UserService.java
- RoleService.java
- UserTrackingService.java
- UserTrackingDataService.java
- ClientInfoService.java
- GeolocationService.java
- ReverseGeocodingService.java

### 2.7 Copy Controllers

Copy từ `backend/src/main/java/com/hust/thailq/controller/`:
- AuthController.java
- AdminUserController.java

### 2.8 Copy Config

Copy từ `backend/src/main/java/com/hust/thailq/config/`:
- SecurityConfig.java
- MessageSourceConfig.java
- AppConfig.java
- ClockConfig.java
- OpenApiConfig.java

**Update SecurityConfig.java**:
```java
private static final String[] AUTH_WHITELIST = {
    "/api/v1/auth/**",
    "/v3/api-docs/**",
    "/swagger-ui/**",
    "/actuator/**"
};
```

### 2.9 Copy Database Migrations

Copy từ `backend/src/main/resources/db/migration/`:
- V1__db_init.sql (chỉ giữ user, role, user_role tables)
- V2__add_user_data.sql
- V3__add_role_data.sql
- V4__add_user_role_data.sql
- V7__create_tracking_tables.sql
- V8__add_sample_tracking_data.sql
- V9__user_session_db_timestamp.sql
- V10__update_user_data.sql
- V18__add_accountant_role.sql
- V19__add_customer_role.sql

**Update V1__db_init.sql** - Remove wallet và transaction tables, chỉ giữ:
```sql
CREATE SEQUENCE IF NOT EXISTS role_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS public.user_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE role (
    id   BIGINT      NOT NULL,
    type VARCHAR(20) NOT NULL,
    CONSTRAINT pk_role PRIMARY KEY (id)
);

CREATE TABLE public."user" (
    id         BIGINT       NOT NULL,
    first_name VARCHAR(50)  NOT NULL,
    last_name  VARCHAR(50)  NOT NULL,
    username   VARCHAR(20)  NOT NULL,
    email      VARCHAR(50)  NOT NULL,
    password   VARCHAR(100) NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE TABLE public.user_role (
    role_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT pk_user_role PRIMARY KEY (role_id, user_id)
);

-- Add constraints and indexes
```

### 2.10 Create Main Application Class

```java
package com.hust.thailq.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.hust.thailq.user", "com.hust.thailq.common"})
@EntityScan(basePackages = "com.hust.thailq.user.domain.entity")
public class UserManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserManagementApplication.class, args);
    }
}
```

### 2.11 Create Dockerfile

```dockerfile
FROM maven:3.9.9-eclipse-temurin-25-alpine AS builder
WORKDIR /app
COPY ../common-service ../common-service
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 💰 Bước 3: Wallet Management Service

### 3.1 Structure

```
wallet-management-service/
├── pom.xml (tương tự user-management-service)
└── src/
    ├── main/
    │   ├── java/com/hust/thailq/wallet/
    │   │   ├── WalletManagementApplication.java
    │   │   ├── config/
    │   │   ├── controller/
    │   │   │   ├── WalletController.java
    │   │   │   └── AdminWalletController.java
    │   │   ├── domain/
    │   │   │   ├── entity/
    │   │   │   │   ├── Wallet.java
    │   │   │   │   └── FraudRuleConfig.java
    │   │   │   └── enums/
    │   │   │       └── WalletStatus.java
    │   │   ├── dto/
    │   │   ├── repository/
    │   │   │   ├── WalletRepository.java
    │   │   │   └── FraudRuleConfigRepository.java
    │   │   ├── service/
    │   │   │   ├── WalletService.java
    │   │   │   ├── FraudDetectionService.java
    │   │   │   └── IbanAnalysisService.java
    │   │   └── client/
    │   │       └── UserServiceClient.java
    │   └── resources/
    │       ├── application.yml
    │       ├── messages.properties
    │       └── db/migration/
    │           ├── V1__create_wallet_table.sql
    │           ├── V11__create_fraud_rule_config_table.sql
    │           ├── V12__add_status_to_wallets.sql
    │           ├── V13__add_bank_info_to_wallet.sql
    │           ├── V14__set_wallet_id_sequence.sql
    │           └── V16__add_init_wallet_type.sql
    └── test/
```

### 3.2 Key Changes

**Wallet.java** - Remove User relationship:
```java
// CHANGE FROM:
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;

// TO:
@Column(name = "user_id", nullable = false)
private Long userId;
```

**Remove Transaction relationships**:
```java
// REMOVE:
@OneToMany(mappedBy = "fromWallet", cascade = CascadeType.ALL, orphanRemoval = true)
private Set<Transaction> sentTransactions = new HashSet<>();

@OneToMany(mappedBy = "toWallet", cascade = CascadeType.ALL, orphanRemoval = true)
private Set<Transaction> receivedTransactions = new HashSet<>();
```

### 3.3 UserServiceClient

```java
@Service
@RequiredArgsConstructor
public class UserServiceClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${services.user.url}")
    private String userServiceUrl;
    
    public UserResponse getUserById(Long userId) {
        try {
            String url = userServiceUrl + "/api/v1/users/" + userId;
            return restTemplate.getForObject(url, UserResponse.class);
        } catch (Exception e) {
            throw new ServiceCommunicationException("Failed to communicate with User Service", e);
        }
    }
    
    public boolean userExists(Long userId) {
        try {
            getUserById(userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 3.4 application.yml

```yaml
server:
  port: 8082

spring:
  application:
    name: wallet-management-service
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5433/wallet_management_db}
    username: ${SPRING_DATASOURCE_USERNAME:postgres}
    password: ${SPRING_DATASOURCE_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration

services:
  user:
    url: ${USER_SERVICE_URL:http://localhost:8081}
```

## 📊 Bước 4: Transaction Management Service

### 4.1 Structure

```
transaction-management-service/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/hust/thailq/transaction/
    │   │   ├── TransactionManagementApplication.java
    │   │   ├── controller/
    │   │   │   ├── TransactionController.java
    │   │   │   └── AdminTransactionController.java
    │   │   ├── domain/
    │   │   │   ├── entity/
    │   │   │   │   ├── Transaction.java
    │   │   │   │   └── Type.java
    │   │   │   └── enums/
    │   │   │       └── Status.java
    │   │   ├── dto/
    │   │   ├── repository/
    │   │   ├── service/
    │   │   │   ├── TransactionService.java
    │   │   │   └── TypeService.java
    │   │   └── client/
    │   │       └── WalletServiceClient.java
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/
    │           ├── V1__create_transaction_tables.sql
    │           ├── V5__add_type_data.sql
    │           ├── V15__add_addfunds_and_withdraw_types.sql
    │           └── V17__add_tracking_fields_to_transaction.sql
    └── test/
```

### 4.2 Key Changes

**Transaction.java** - Remove Wallet relationships:
```java
// CHANGE FROM:
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "from_wallet_id")
private Wallet fromWallet;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "to_wallet_id")
private Wallet toWallet;

// TO:
@Column(name = "from_wallet_id", nullable = false)
private Long fromWalletId;

@Column(name = "to_wallet_id", nullable = false)
private Long toWalletId;
```

### 4.3 application.yml

```yaml
server:
  port: 8083

spring:
  application:
    name: transaction-management-service
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5434/transaction_management_db}
    username: ${SPRING_DATASOURCE_USERNAME:postgres}
    password: ${SPRING_DATASOURCE_PASSWORD:postgres}

services:
  wallet:
    url: ${WALLET_SERVICE_URL:http://localhost:8082}
```

## 💳 Bước 5: Payment Management Service

### 5.1 Structure

```
payment-management-service/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/hust/thailq/payment/
    │   │   ├── PaymentManagementApplication.java
    │   │   ├── controller/
    │   │   │   ├── AdminTrackingController.java
    │   │   │   └── FraudRuleConfigController.java
    │   │   ├── service/
    │   │   │   └── PaymentService.java (orchestrates wallet + transaction)
    │   │   └── client/
    │   │       ├── WalletServiceClient.java
    │   │       └── TransactionServiceClient.java
    │   └── resources/
    │       └── application.yml
    └── test/
```

### 5.2 PaymentService - Orchestration Pattern

```java
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    
    private final WalletServiceClient walletClient;
    private final TransactionServiceClient transactionClient;
    
    public TransactionResponse transfer(TransactionRequest request) {
        // 1. Validate wallets exist
        WalletResponse fromWallet = walletClient.getWalletById(request.getFromWalletId());
        WalletResponse toWallet = walletClient.getWalletById(request.getToWalletId());
        
        // 2. Check balance
        if (fromWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }
        
        // 3. Update wallet balances
        walletClient.updateBalance(request.getFromWalletId(), 
            fromWallet.getBalance().subtract(request.getAmount()));
        walletClient.updateBalance(request.getToWalletId(), 
            toWallet.getBalance().add(request.getAmount()));
        
        // 4. Create transaction record
        return transactionClient.createTransaction(request);
    }
}
```

### 5.3 application.yml

```yaml
server:
  port: 8084

spring:
  application:
    name: payment-management-service
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5435/payment_management_db}
    username: ${SPRING_DATASOURCE_USERNAME:postgres}
    password: ${SPRING_DATASOURCE_PASSWORD:postgres}

services:
  wallet:
    url: ${WALLET_SERVICE_URL:http://localhost:8082}
  transaction:
    url: ${TRANSACTION_SERVICE_URL:http://localhost:8083}
```

## 🔧 Bước 6: RestTemplate Configuration

Tất cả services cần có RestTemplate config:

```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(5))
            .build();
    }
}
```

## 🔐 Bước 7: Global Exception Handler

Mỗi service cần có GlobalExceptionHandler:

```java
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    
    private final MessageSourceConfig messageConfig;
    
    @Value("${exception.trace:false}")
    private boolean printStackTrace;
    
    // Copy implementation từ backend/src/main/java/com/hust/thailq/exception/GlobalExceptionHandler.java
}
```

## 🌐 Bước 8: Update Frontend

### 8.1 Update axios.js

```javascript
import axios from "axios";

const instance = axios.create({ 
    baseURL: "http://localhost:8080/api/v1"  // API Gateway URL
});

instance.defaults.headers.common["Content-Type"] = "application/json";

instance.interceptors.request.use(
  (config) => {
    const user = JSON.parse(localStorage.getItem('user'));
    if (user && user.token) {
      config.headers.Authorization = `Bearer ${user.token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default instance;
```

### 8.2 No other changes needed

Frontend không cần thay đổi gì khác vì API Gateway routing tự động.

## 🗄️ Bước 9: Database Setup

### 9.1 Create Databases

```sql
CREATE DATABASE user_management_db;
CREATE DATABASE wallet_management_db;
CREATE DATABASE transaction_management_db;
CREATE DATABASE payment_management_db;
```

### 9.2 Migrate Data (nếu có data cũ)

```bash
# Export từ monolithic database
pg_dump -t user -t role -t user_role -t user_session -t user_activity monolithic_db > user_data.sql
pg_dump -t wallet -t fraud_rule_config monolithic_db > wallet_data.sql
pg_dump -t transaction -t type monolithic_db > transaction_data.sql

# Import vào microservices databases
psql user_management_db < user_data.sql
psql wallet_management_db < wallet_data.sql
psql transaction_management_db < transaction_data.sql
```

## 🚀 Bước 10: Build và Run

### 10.1 Build All

```bash
cd microservices
mvn clean install
```

### 10.2 Run with Docker Compose

```bash
cd microservices
docker-compose up --build
```

### 10.3 Run Individually (for development)

```bash
# Terminal 1 - User Service
cd user-management-service
mvn spring-boot:run

# Terminal 2 - Wallet Service
cd wallet-management-service
mvn spring-boot:run

# Terminal 3 - Transaction Service
cd transaction-management-service
mvn spring-boot:run

# Terminal 4 - Payment Service
cd payment-management-service
mvn spring-boot:run

# Terminal 5 - API Gateway
cd api-gateway
mvn spring-boot:run
```

## ✅ Bước 11: Testing

### 11.1 Health Checks

```bash
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Wallet Service
curl http://localhost:8083/actuator/health  # Transaction Service
curl http://localhost:8084/actuator/health  # Payment Service
curl http://localhost:8080/actuator/health  # API Gateway
```

### 11.2 Test Authentication

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 11.3 Test Wallet Creation

```bash
curl -X POST http://localhost:8080/api/v1/wallets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"name":"My Wallet","iban":"DE89370400440532013000"}'
```

## 📊 Bước 12: Monitoring

### 12.1 Swagger UI

- User Service: http://localhost:8081/swagger-ui.html
- Wallet Service: http://localhost:8082/swagger-ui.html
- Transaction Service: http://localhost:8083/swagger-ui.html
- Payment Service: http://localhost:8084/swagger-ui.html

### 12.2 Actuator Endpoints

- Health: `/actuator/health`
- Info: `/actuator/info`
- Metrics: `/actuator/metrics`

## 🎯 Best Practices Checklist

- [ ] Sử dụng DTOs thay vì expose entities
- [ ] Validate input với @Valid
- [ ] Handle exceptions với GlobalExceptionHandler
- [ ] Log với SLF4J (@Slf4j)
- [ ] Use final cho immutable variables
- [ ] Use Optional<T> thay vì null
- [ ] Transaction management với @Transactional
- [ ] Database migration với Flyway
- [ ] API documentation với Swagger
- [ ] Health checks với Actuator
- [ ] CORS configuration
- [ ] JWT authentication
- [ ] Password encryption với BCrypt
- [ ] Input sanitization
- [ ] SQL injection prevention (JPA)
- [ ] XSS prevention
- [ ] CSRF protection

## 🔍 Troubleshooting

### Issue: Service không start

**Solution**: Check logs, verify database connection, ensure port không bị conflict

### Issue: Inter-service communication failed

**Solution**: Verify service URLs, check network connectivity, ensure services đang chạy

### Issue: JWT validation failed

**Solution**: Ensure tất cả services dùng chung jwtSecret

### Issue: Database migration failed

**Solution**: Check Flyway scripts, verify database permissions, check version conflicts

## 📚 Resources

- Spring Boot Documentation: https://spring.io/projects/spring-boot
- Spring Cloud Gateway: https://spring.io/projects/spring-cloud-gateway
- Flyway: https://flywaydb.org/
- Docker Compose: https://docs.docker.com/compose/
- PostgreSQL: https://www.postgresql.org/docs/

## 🎓 Next Steps

1. Implement Circuit Breaker (Resilience4j)
2. Add Distributed Tracing (Zipkin/Jaeger)
3. Implement API Rate Limiting
4. Add Caching (Redis)
5. Implement Event-Driven Architecture (Kafka/RabbitMQ)
6. Add Service Mesh (Istio)
7. Implement CQRS pattern
8. Add Saga pattern for distributed transactions
