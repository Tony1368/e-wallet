# E-Wallet Microservices Architecture

## 📋 Tổng quan

Dự án đã được chuyển đổi từ kiến trúc Monolithic sang Microservices với các service sau:

### Các Microservices

1. **user-management-service** (Port: 8081)
   - Quản lý user, authentication, authorization
   - Database: `user_management_db`
   - Endpoints: `/api/v1/auth/**`, `/api/v1/users/**`, `/api/v1/admin/users/**`

2. **wallet-management-service** (Port: 8082)
   - Quản lý ví điện tử
   - Database: `wallet_management_db`
   - Endpoints: `/api/v1/wallets/**`, `/api/v1/admin/wallets/**`

3. **transaction-management-service** (Port: 8083)
   - Quản lý giao dịch và lịch sử
   - Database: `transaction_management_db`
   - Endpoints: `/api/v1/transactions/**`, `/api/v1/admin/transactions/**`

4. **payment-management-service** (Port: 8084)
   - Xử lý thanh toán và chuyển tiền
   - Database: `payment_management_db`
   - Endpoints: `/api/v1/payments/**`, `/api/v1/transfers/**`

5. **api-gateway** (Port: 8080)
   - API Gateway routing tất cả requests
   - Load balancing và service discovery

6. **common-service**
   - Shared libraries, DTOs, exceptions, validators

## 🏗️ Kiến trúc

```
┌─────────────┐
│   Frontend  │
│  (Port 3000)│
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────┐
│        API Gateway (8080)           │
│  - Routing                          │
│  - Load Balancing                   │
│  - CORS Configuration               │
└──────┬──────────────────────────────┘
       │
       ├──────────────┬──────────────┬──────────────┬──────────────┐
       ▼              ▼              ▼              ▼              ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│   User      │ │   Wallet    │ │ Transaction │ │  Payment    │
│ Management  │ │ Management  │ │ Management  │ │ Management  │
│  (8081)     │ │  (8082)     │ │  (8083)     │ │  (8084)     │
└──────┬──────┘ └──────┬──────┘ └──────┬──────┘ └──────┬──────┘
       │               │               │               │
       ▼               ▼               ▼               ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│   User DB   │ │  Wallet DB  │ │Transaction  │ │  Payment DB │
│             │ │             │ │     DB      │ │             │
└─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
```

## 📦 Cấu trúc thư mục

```
microservices/
├── pom.xml (Parent POM)
├── common-service/
│   ├── pom.xml
│   └── src/main/java/com/hust/thailq/
│       ├── common/
│       │   ├── Constants.java
│       │   └── MessageKeys.java
│       ├── dto/
│       │   └── CommandResponse.java
│       ├── exception/
│       │   ├── ElementAlreadyExistsException.java
│       │   ├── NoSuchElementFoundException.java
│       │   ├── InsufficientFundsException.java
│       │   ├── ServiceCommunicationException.java
│       │   └── ErrorResponse.java
│       └── validator/
│           ├── IbanValidator.java
│           └── ValidIban.java
│
├── user-management-service/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/hust/thailq/user/
│       │   │   ├── UserManagementApplication.java
│       │   │   ├── config/
│       │   │   ├── controller/
│       │   │   ├── domain/
│       │   │   ├── dto/
│       │   │   ├── repository/
│       │   │   ├── security/
│       │   │   └── service/
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── messages.properties
│       │       └── db/migration/
│       └── test/
│
├── wallet-management-service/
│   ├── pom.xml
│   └── src/ (tương tự user-management-service)
│
├── transaction-management-service/
│   ├── pom.xml
│   └── src/ (tương tự user-management-service)
│
├── payment-management-service/
│   ├── pom.xml
│   └── src/ (tương tự user-management-service)
│
└── api-gateway/
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/hust/thailq/gateway/
        │   │   ├── ApiGatewayApplication.java
        │   │   └── config/
        │   └── resources/
        │       └── application.yml
        └── test/
```

## 🗄️ Phân tách Database

### 1. User Management Database

```sql
-- user_management_db
CREATE DATABASE user_management_db;

-- Tables:
- user (id, first_name, last_name, username, email, password)
- role (id, type)
- user_role (user_id, role_id)
- user_session (tracking login sessions)
- user_activity (tracking user activities)
```

### 2. Wallet Management Database

```sql
-- wallet_management_db
CREATE DATABASE wallet_management_db;

-- Tables:
- wallet (id, user_id, name, iban, balance, status, bank_info, created_at)
- fraud_rule_config (fraud detection rules)
```

### 3. Transaction Management Database

```sql
-- transaction_management_db
CREATE DATABASE transaction_management_db;

-- Tables:
- transaction (id, amount, description, created_at, reference_number, status, 
               from_wallet_id, to_wallet_id, type_id, tracking fields...)
- type (id, name, description)
```

### 4. Payment Management Database

```sql
-- payment_management_db
CREATE DATABASE payment_management_db;

-- Tables:
- payment (payment specific data)
- transfer (transfer specific data)
```

## 🔧 Cấu hình

### API Gateway Configuration (application.yml)

```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-management-service
          predicates:
            - Path=/api/v1/auth/**, /api/v1/users/**, /api/v1/admin/users/**
          filters:
            - RewritePath=/api/v1/(?<segment>.*), /api/v1/$\{segment}
            
        - id: wallet-service
          uri: lb://wallet-management-service
          predicates:
            - Path=/api/v1/wallets/**, /api/v1/admin/wallets/**
          filters:
            - RewritePath=/api/v1/(?<segment>.*), /api/v1/$\{segment}
            
        - id: transaction-service
          uri: lb://transaction-management-service
          predicates:
            - Path=/api/v1/transactions/**, /api/v1/admin/transactions/**
          filters:
            - RewritePath=/api/v1/(?<segment>.*), /api/v1/$\{segment}
            
        - id: payment-service
          uri: lb://payment-management-service
          predicates:
            - Path=/api/v1/payments/**, /api/v1/transfers/**
          filters:
            - RewritePath=/api/v1/(?<segment>.*), /api/v1/$\{segment}
      
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "http://localhost:3000"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

### User Management Service Configuration

```yaml
server:
  port: 8081

spring:
  application:
    name: user-management-service
  datasource:
    url: jdbc:postgresql://localhost:5432/user_management_db
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  flyway:
    enabled: true
    locations: classpath:db/migration

app:
  security:
    jwtSecret: YourSecretKeyHere
    jwtExpirationMs: 86400000

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

## 🚀 Hướng dẫn Build và Deploy

### 1. Build tất cả services

```bash
cd microservices
mvn clean install
```

### 2. Build từng service riêng lẻ

```bash
# Common Service
cd common-service
mvn clean install

# User Management Service
cd ../user-management-service
mvn clean package

# Wallet Management Service
cd ../wallet-management-service
mvn clean package

# Transaction Management Service
cd ../transaction-management-service
mvn clean package

# Payment Management Service
cd ../payment-management-service
mvn clean package

# API Gateway
cd ../api-gateway
mvn clean package
```

### 3. Chạy services

```bash
# Start User Management Service
java -jar user-management-service/target/user-management-service-1.0.0.jar

# Start Wallet Management Service
java -jar wallet-management-service/target/wallet-management-service-1.0.0.jar

# Start Transaction Management Service
java -jar transaction-management-service/target/transaction-management-service-1.0.0.jar

# Start Payment Management Service
java -jar payment-management-service/target/payment-management-service-1.0.0.jar

# Start API Gateway
java -jar api-gateway/target/api-gateway-1.0.0.jar
```

## 🐳 Docker Deployment

### Docker Compose

```yaml
version: '3.8'

services:
  # Databases
  user-db:
    image: postgres:16
    environment:
      POSTGRES_DB: user_management_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - user-db-data:/var/lib/postgresql/data

  wallet-db:
    image: postgres:16
    environment:
      POSTGRES_DB: wallet_management_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5433:5432"
    volumes:
      - wallet-db-data:/var/lib/postgresql/data

  transaction-db:
    image: postgres:16
    environment:
      POSTGRES_DB: transaction_management_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5434:5432"
    volumes:
      - transaction-db-data:/var/lib/postgresql/data

  payment-db:
    image: postgres:16
    environment:
      POSTGRES_DB: payment_management_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5435:5432"
    volumes:
      - payment-db-data:/var/lib/postgresql/data

  # Microservices
  user-service:
    build: ./user-management-service
    ports:
      - "8081:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://user-db:5432/user_management_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
    depends_on:
      - user-db

  wallet-service:
    build: ./wallet-management-service
    ports:
      - "8082:8082"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://wallet-db:5432/wallet_management_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
    depends_on:
      - wallet-db

  transaction-service:
    build: ./transaction-management-service
    ports:
      - "8083:8083"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://transaction-db:5432/transaction_management_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
    depends_on:
      - transaction-db

  payment-service:
    build: ./payment-management-service
    ports:
      - "8084:8084"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://payment-db:5432/payment_management_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
    depends_on:
      - payment-db

  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    depends_on:
      - user-service
      - wallet-service
      - transaction-service
      - payment-service

volumes:
  user-db-data:
  wallet-db-data:
  transaction-db-data:
  payment-db-data:
```

## 🔄 Inter-Service Communication

### RestTemplate Configuration

Mỗi service cần có RestTemplate để gọi các service khác:

```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### Service Client Example

```java
@Service
@RequiredArgsConstructor
public class WalletServiceClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${services.wallet.url}")
    private String walletServiceUrl;
    
    public WalletResponse getWalletById(Long walletId) {
        String url = walletServiceUrl + "/api/v1/wallets/" + walletId;
        return restTemplate.getForObject(url, WalletResponse.class);
    }
}
```

## 🔐 Security & JWT

JWT token được tạo ở User Management Service và validate ở tất cả các service khác.

### Shared JWT Configuration

Tất cả services phải dùng chung:
- `jwtSecret`: Secret key để sign và verify token
- `jwtExpirationMs`: Thời gian hết hạn token

## 📝 Best Practices đã áp dụng

### 1. Design Patterns

- **Repository Pattern**: Tách biệt data access logic
- **Service Layer Pattern**: Business logic trong service layer
- **DTO Pattern**: Data Transfer Objects cho API
- **Factory Pattern**: Tạo objects phức tạp
- **Builder Pattern**: Lombok @Builder cho entities
- **Singleton Pattern**: Spring Beans

### 2. Naming Conventions

- **Classes**: PascalCase (UserService, WalletController)
- **Methods**: camelCase (getUserById, createWallet)
- **Constants**: UPPER_SNAKE_CASE (API_VERSION, IBAN_MAX_SIZE)
- **Variables**: camelCase (userId, walletBalance)
- **Packages**: lowercase (com.hust.thailq.user.service)

### 3. Code Quality

- **Immutability**: Sử dụng `final` cho variables không thay đổi
- **Null Safety**: Sử dụng Optional<T> thay vì null
- **Exception Handling**: Global exception handler
- **Validation**: Bean Validation với @Valid
- **Logging**: SLF4J với Lombok @Slf4j
- **Documentation**: JavaDoc cho public methods

### 4. Database Best Practices

- **Flyway Migration**: Version control cho database
- **Indexes**: Đánh index cho foreign keys và search fields
- **Constraints**: NOT NULL, UNIQUE, FOREIGN KEY
- **Sequences**: Sử dụng sequences cho ID generation
- **Transactions**: @Transactional cho business operations

## 🔄 Migration từ Monolithic

### Bước 1: Phân tích Dependencies

Xác định dependencies giữa các modules:
- User → Wallet (One-to-Many)
- Wallet → Transaction (One-to-Many)
- Transaction → Type (Many-to-One)

### Bước 2: Tách Database

Tạo 4 databases riêng biệt và migrate data:

```sql
-- Export data từ monolithic database
pg_dump -t user -t role -t user_role monolithic_db > user_data.sql
pg_dump -t wallet monolithic_db > wallet_data.sql
pg_dump -t transaction -t type monolithic_db > transaction_data.sql

-- Import vào microservices databases
psql user_management_db < user_data.sql
psql wallet_management_db < wallet_data.sql
psql transaction_management_db < transaction_data.sql
```

### Bước 3: Refactor Code

1. Copy entities vào service tương ứng
2. Remove foreign key relationships giữa services
3. Sử dụng user_id, wallet_id thay vì object references
4. Implement service clients cho inter-service communication

### Bước 4: Update Frontend

Thay đổi base URL từ service cụ thể sang API Gateway:

```javascript
// Before
const BASE_URL = "http://localhost:8082/api/v1";

// After
const BASE_URL = "http://localhost:8080/api/v1";
```

## 📊 Monitoring & Logging

### Actuator Endpoints

Mỗi service expose actuator endpoints:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

Access: `http://localhost:8081/actuator/health`

### Centralized Logging

Sử dụng ELK Stack (Elasticsearch, Logstash, Kibana) hoặc Splunk để tập trung logs.

## 🧪 Testing

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void testGetUserById() {
        // Test implementation
    }
}
```

### Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testCreateUser() throws Exception {
        // Test implementation
    }
}
```

## 📚 API Documentation

Mỗi service có Swagger UI:
- User Service: http://localhost:8081/swagger-ui.html
- Wallet Service: http://localhost:8082/swagger-ui.html
- Transaction Service: http://localhost:8083/swagger-ui.html
- Payment Service: http://localhost:8084/swagger-ui.html

## 🔧 Troubleshooting

### Common Issues

1. **Service không kết nối được với database**
   - Kiểm tra connection string
   - Verify database đang chạy
   - Check credentials

2. **Inter-service communication failed**
   - Verify service discovery đang hoạt động
   - Check network connectivity
   - Verify service URLs

3. **JWT validation failed**
   - Ensure all services dùng chung jwtSecret
   - Check token expiration
   - Verify token format

## 📞 Support

Để được hỗ trợ, vui lòng tạo issue trên GitHub repository.

## 📄 License

MIT License
