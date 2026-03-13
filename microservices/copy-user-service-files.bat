@echo off
echo ========================================
echo Copy Files for User Management Service
echo ========================================
echo.

set SRC=..\backend\src\main\java\com\hust\thailq
set DEST=user-management-service\src\main\java\com\hust\thailq\user

echo Creating directories...
if not exist "%DEST%\domain\entity" mkdir "%DEST%\domain\entity"
if not exist "%DEST%\domain\enums" mkdir "%DEST%\domain\enums"
if not exist "%DEST%\dto\request" mkdir "%DEST%\dto\request"
if not exist "%DEST%\dto\response" mkdir "%DEST%\dto\response"
if not exist "%DEST%\dto\mapper" mkdir "%DEST%\dto\mapper"
if not exist "%DEST%\repository" mkdir "%DEST%\repository"
if not exist "%DEST%\security" mkdir "%DEST%\security"
if not exist "%DEST%\service" mkdir "%DEST%\service"
if not exist "%DEST%\controller" mkdir "%DEST%\controller"
if not exist "%DEST%\config" mkdir "%DEST%\config"
if not exist "%DEST%\exception" mkdir "%DEST%\exception"

echo.
echo Copying entities...
copy "%SRC%\domain\entity\User.java" "%DEST%\domain\entity\" >nul
copy "%SRC%\domain\entity\Role.java" "%DEST%\domain\entity\" >nul
copy "%SRC%\domain\entity\UserSession.java" "%DEST%\domain\entity\" >nul
copy "%SRC%\domain\entity\UserActivity.java" "%DEST%\domain\entity\" >nul
echo   - 4 entity files copied

echo Copying enums...
copy "%SRC%\domain\enums\RoleType.java" "%DEST%\domain\enums\" >nul
echo   - 1 enum file copied

echo Copying DTOs...
copy "%SRC%\dto\request\LoginRequest.java" "%DEST%\dto\request\" >nul
copy "%SRC%\dto\request\SignupRequest.java" "%DEST%\dto\request\" >nul
copy "%SRC%\dto\response\JwtResponse.java" "%DEST%\dto\response\" >nul
copy "%SRC%\dto\response\UserResponse.java" "%DEST%\dto\response\" >nul
copy "%SRC%\dto\response\RoleResponse.java" "%DEST%\dto\response\" >nul
copy "%SRC%\dto\response\UserSessionResponse.java" "%DEST%\dto\response\" >nul
copy "%SRC%\dto\response\UserActivityResponse.java" "%DEST%\dto\response\" >nul
echo   - 7 DTO files copied

echo Copying mappers...
copy "%SRC%\dto\mapper\SignupRequestMapper.java" "%DEST%\dto\mapper\" >nul
copy "%SRC%\dto\mapper\UserResponseMapper.java" "%DEST%\dto\mapper\" >nul
copy "%SRC%\dto\mapper\UserSessionResponseMapper.java" "%DEST%\dto\mapper\" >nul
copy "%SRC%\dto\mapper\UserActivityResponseMapper.java" "%DEST%\dto\mapper\" >nul
echo   - 4 mapper files copied

echo Copying repositories...
copy "%SRC%\repository\UserRepository.java" "%DEST%\repository\" >nul
copy "%SRC%\repository\RoleRepository.java" "%DEST%\repository\" >nul
copy "%SRC%\repository\UserSessionRepository.java" "%DEST%\repository\" >nul
copy "%SRC%\repository\UserActivityRepository.java" "%DEST%\repository\" >nul
echo   - 4 repository files copied

echo Copying security...
copy "%SRC%\security\AuthTokenFilter.java" "%DEST%\security\" >nul
copy "%SRC%\security\AuthEntryPointJwt.java" "%DEST%\security\" >nul
copy "%SRC%\security\UserDetailsImpl.java" "%DEST%\security\" >nul
copy "%SRC%\security\UserDetailsServiceImpl.java" "%DEST%\security\" >nul
echo   - 4 security files copied
echo   - Note: JwtUtils.java already updated in microservices

echo Copying services...
copy "%SRC%\service\AuthService.java" "%DEST%\service\" >nul
copy "%SRC%\service\UserService.java" "%DEST%\service\" >nul
copy "%SRC%\service\RoleService.java" "%DEST%\service\" >nul
copy "%SRC%\service\UserTrackingService.java" "%DEST%\service\" >nul
copy "%SRC%\service\UserTrackingDataService.java" "%DEST%\service\" >nul
copy "%SRC%\service\ClientInfoService.java" "%DEST%\service\" >nul
copy "%SRC%\service\GeolocationService.java" "%DEST%\service\" >nul
copy "%SRC%\service\ReverseGeocodingService.java" "%DEST%\service\" >nul
echo   - 8 service files copied

echo Copying controllers...
copy "%SRC%\controller\AuthController.java" "%DEST%\controller\" >nul
copy "%SRC%\controller\AdminUserController.java" "%DEST%\controller\" >nul
echo   - 2 controller files copied

echo Copying config...
copy "%SRC%\config\SecurityConfig.java" "%DEST%\config\" >nul
copy "%SRC%\config\MessageSourceConfig.java" "%DEST%\config\" >nul
copy "%SRC%\config\AppConfig.java" "%DEST%\config\" >nul
copy "%SRC%\config\ClockConfig.java" "%DEST%\config\" >nul
copy "%SRC%\config\OpenApiConfig.java" "%DEST%\config\" >nul
echo   - 5 config files copied

echo Copying exception handler...
copy "%SRC%\exception\GlobalExceptionHandler.java" "%DEST%\exception\" >nul
echo   - 1 exception handler copied

echo.
echo ========================================
echo FILES COPIED SUCCESSFULLY!
echo ========================================
echo.
echo Total: 40+ files copied
echo.
echo IMPORTANT NEXT STEPS:
echo.
echo 1. Fix package names in all copied files:
echo    Find:    package com.hust.thailq.
echo    Replace: package com.hust.thailq.user.
echo.
echo 2. Fix imports in all files:
echo    Find:    import com.hust.thailq.domain
echo    Replace: import com.hust.thailq.user.domain
echo.
echo 3. Edit User.java - REMOVE Wallet relationship:
echo    - Remove @OneToMany wallets field
echo    - Remove addWallet() and removeWallet() methods
echo.
echo 4. Copy JwtUtils.java from backend\src\main\java\com\hust\thailq\security\
echo    to user-management-service\src\main\java\com\hust\thailq\user\security\
echo    (This file was already updated for jjwt 0.12.6)
echo.
echo 5. Update UserManagementApplication.java with @ComponentScan
echo.
echo 6. Rebuild: cd user-management-service ^&^& mvn clean package
echo.
echo 7. Run: java -jar target\user-management-service-1.0.0.jar
echo.
echo See FILE_COPY_CHECKLIST.md for detailed instructions
echo.

pause
