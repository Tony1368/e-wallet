# 📋 File Copy Checklist

## User Management Service (✅ Ready to implement)

### Entities
```bash
# Copy from: backend\src\main\java\com\hust\thailq\domain\entity\
# To: microservices\user-management-service\src\main\java\com\hust\thailq\user\domain\entity\

- [ ] User.java (REMOVE @OneToMany wallets relationship)
- [ ] Role.java
- [ ] UserSession.java
- [ ] UserActivity.java
```

### Enums
```bash
# Copy from: backend\src\main\java\com\hust\thailq\domain\enums\
# To: microservices\user-management-service\src\main\java\com\hust\thailq\user\domain\enums\

- [ ] RoleType.java
```

### DTOs
```bash
# Copy from: backend\src\main\java\com\hust\thailq\dto\
# To: microservices\user-management-service\src\main\java\com\hust\thailq\user\dto\

Request:
- [ ] LoginRequest.java
- [ ] SignupRequest.java

Response:
- [ ] JwtResponse.java
- [ ] UserResponse.java
- [ ] RoleResponse.java
- [ ] UserSessionResponse.java
- [ ] UserActivityResponse.java

Mappers:
- [ ] SignupRequestMapper.java
- [ ] UserResponseMapper.java
- [ ] UserSessionResponseMapper.java
- [ ] UserActivityResponseMapper.java
```

### Repositories
```bash
# Copy from: backend\src\main\java\com\hust\thailq\repository\
# To: microservices\user-management-service\src\main\java\com\hust\thailq\user\repository\

- [ ] UserRepository.java
- [ ] RoleRepository.java
- [ ] UserSessionRepository.java
- [ ] UserActivityRepository.java
```

### Security
```bash
# Copy from: backend\src\main\java\com\hust\thailq\security\
# To: microservices\user-management-service\src\main\java\com\hust\thailq\user\security\

- [ ] JwtUtils.java (already updated for jjwt 0.12.6)
- [ ] AuthTokenFilter.java
- [ ] AuthEntryPointJwt.java
- [ ] UserDetailsImpl.java
- [ ] UserDetailsServiceImpl.java
```

### Services
```bash
# Copy from: backend\src\main\java\com\hust\thailq\service\
# To: microservices\user-management-service\src\main\java\com\hust\thailq\user\service\

- [ ] AuthService.java
- [ ] UserService.java
- [ ] RoleService.java
- [ ] UserTrackingService.java
- [ ] UserTrackingDataService.java
- [ ] ClientInfoService.java
- [ ] GeolocationService.java
- [ ] ReverseGeocodingService.java
```

### Controllers
```bash
# Copy from: backend\src\main\java\com\hust\thailq\controller\
# To: microservices\user-management-service\src\main\java\com\hust\thailq\user\controller\

- [ ] AuthController.java
- [ ] AdminUserController.java
```

### Config
```bash
# Copy from: backend\src\main\java\com\hust\thailq\config\
# To: microservices\user-management-service\src\main\java\com\hust\thailq\user\config\

- [ ] SecurityConfig.java
- [ ] MessageSourceConfig.java
- [ ] AppConfig.java
- [ ] ClockConfig.java
- [ ] OpenApiConfig.java
```

### Exception Handler
```bash
# Copy from: backend\src\main\java\com\hust\thailq\exception\
# To: microservices\user-management-service\src\main\java\com\hust\thailq\user\exception\

- [ ] GlobalExceptionHandler.java
```

### Database Migrations
```bash
# Already created in:
# microservices\user-management-service\src\main\resources\db\migration\

✅ V1__user_db_init.sql
✅ V2__add_user_data.sql
✅ V3__add_role_data.sql
✅ V4__add_user_role_data.sql
✅ V5__create_tracking_tables.sql
```

## Important Changes for User.java

```java
// REMOVE THIS:
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
private Set<Wallet> wallets = new HashSet<>();

public void addWallet(Wallet wallet) {
    wallets.add(wallet);
    wallet.setUser(this);
}

public void removeWallet(Wallet wallet) {
    wallets.remove(wallet);
    wallet.setUser(null);
}
```

## Wallet Management Service (Next)

### Entities
```bash
- [ ] Wallet.java (CHANGE @ManyToOne User to Long userId)
- [ ] FraudRuleConfig.java
```

### Key Change for Wallet.java

```java
// CHANGE FROM:
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;

// TO:
@Column(name = "user_id", nullable = false)
private Long userId;

// REMOVE:
@OneToMany(mappedBy = "fromWallet", ...)
private Set<Transaction> sentTransactions = new HashSet<>();

@OneToMany(mappedBy = "toWallet", ...)
private Set<Transaction> receivedTransactions = new HashSet<>();
```

### Service Client
```bash
# Create new:
# microservices\wallet-management-service\src\main\java\com\hust\thailq\wallet\client\

- [ ] UserServiceClient.java (to call User Service)
```

## Quick Copy Commands (Windows)

```batch
@echo off
REM User Management Service - Copy Entities

set SRC=backend\src\main\java\com\hust\thailq
set DEST=microservices\user-management-service\src\main\java\com\hust\thailq\user

REM Create directories
mkdir %DEST%\domain\entity
mkdir %DEST%\domain\enums
mkdir %DEST%\dto\request
mkdir %DEST%\dto\response
mkdir %DEST%\dto\mapper
mkdir %DEST%\repository
mkdir %DEST%\security
mkdir %DEST%\service
mkdir %DEST%\controller
mkdir %DEST%\config
mkdir %DEST%\exception

REM Copy entities
copy %SRC%\domain\entity\User.java %DEST%\domain\entity\
copy %SRC%\domain\entity\Role.java %DEST%\domain\entity\
copy %SRC%\domain\entity\UserSession.java %DEST%\domain\entity\
copy %SRC%\domain\entity\UserActivity.java %DEST%\domain\entity\

REM Copy enums
copy %SRC%\domain\enums\RoleType.java %DEST%\domain\enums\

REM Copy DTOs
copy %SRC%\dto\request\LoginRequest.java %DEST%\dto\request\
copy %SRC%\dto\request\SignupRequest.java %DEST%\dto\request\
copy %SRC%\dto\response\JwtResponse.java %DEST%\dto\response\
copy %SRC%\dto\response\UserResponse.java %DEST%\dto\response\
copy %SRC%\dto\response\RoleResponse.java %DEST%\dto\response\
copy %SRC%\dto\response\UserSessionResponse.java %DEST%\dto\response\
copy %SRC%\dto\response\UserActivityResponse.java %DEST%\dto\response\

REM Copy mappers
copy %SRC%\dto\mapper\SignupRequestMapper.java %DEST%\dto\mapper\
copy %SRC%\dto\mapper\UserResponseMapper.java %DEST%\dto\mapper\
copy %SRC%\dto\mapper\UserSessionResponseMapper.java %DEST%\dto\mapper\
copy %SRC%\dto\mapper\UserActivityResponseMapper.java %DEST%\dto\mapper\

REM Copy repositories
copy %SRC%\repository\UserRepository.java %DEST%\repository\
copy %SRC%\repository\RoleRepository.java %DEST%\repository\
copy %SRC%\repository\UserSessionRepository.java %DEST%\repository\
copy %SRC%\repository\UserActivityRepository.java %DEST%\repository\

REM Copy security
copy %SRC%\security\*.java %DEST%\security\

REM Copy services
copy %SRC%\service\AuthService.java %DEST%\service\
copy %SRC%\service\UserService.java %DEST%\service\
copy %SRC%\service\RoleService.java %DEST%\service\
copy %SRC%\service\UserTrackingService.java %DEST%\service\
copy %SRC%\service\UserTrackingDataService.java %DEST%\service\
copy %SRC%\service\ClientInfoService.java %DEST%\service\
copy %SRC%\service\GeolocationService.java %DEST%\service\
copy %SRC%\service\ReverseGeocodingService.java %DEST%\service\

REM Copy controllers
copy %SRC%\controller\AuthController.java %DEST%\controller\
copy %SRC%\controller\AdminUserController.java %DEST%\controller\

REM Copy config
copy %SRC%\config\*.java %DEST%\config\

REM Copy exception handler
copy %SRC%\exception\GlobalExceptionHandler.java %DEST%\exception\

echo Files copied successfully!
echo.
echo IMPORTANT: Edit User.java to remove Wallet relationship!
pause
```

Save this as `copy-user-service-files.bat` in the root directory.

## After Copying Files

### 1. Fix Package Names

All copied files will have wrong package names. Need to change:

```java
// FROM:
package com.hust.thailq.domain.entity;

// TO:
package com.hust.thailq.user.domain.entity;
```

Use Find & Replace in IDE:
- Find: `package com.hust.thailq.`
- Replace: `package com.hust.thailq.user.`

### 2. Fix Imports

Update imports to use new package structure:
```java
// FROM:
import com.hust.thailq.domain.entity.User;

// TO:
import com.hust.thailq.user.domain.entity.User;
```

### 3. Update UserManagementApplication.java

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

### 4. Rebuild

```bash
cd microservices\user-management-service
mvn clean package
```

### 5. Run

```bash
java -jar target\user-management-service-1.0.0.jar
```

## Verification Checklist

- [ ] Service starts without errors
- [ ] Flyway migrations run successfully
- [ ] Can access Swagger UI: http://localhost:8081/swagger-ui.html
- [ ] Can login via API: POST /api/v1/auth/login
- [ ] Can get users: GET /api/v1/users
- [ ] Health check works: http://localhost:8081/actuator/health

## Need Help?

See detailed guide in: **IMPLEMENTATION_GUIDE.md**
