# ⚠️ Java Version Change: JDK 25 → JDK 21

## Lý do thay đổi

Ban đầu dự án được nâng cấp lên JDK 25, nhưng gặp lỗi khi build do:
- Lombok 1.18.36 chưa hỗ trợ đầy đủ JDK 25
- JDK 25 là early-access release, chưa stable
- Nhiều libraries chưa tương thích với JDK 25

## Giải pháp

Đã downgrade xuống **JDK 21 (LTS - Long Term Support)** vì:
- ✅ Stable và production-ready
- ✅ Long Term Support từ Oracle
- ✅ Tất cả libraries đều hỗ trợ tốt
- ✅ Spring Boot 3.4.1 hỗ trợ đầy đủ
- ✅ Lombok 1.18.36 hoạt động hoàn hảo

## Các thay đổi đã thực hiện

### 1. Backend Monolithic
- ✅ pom.xml: `java.version` = 21
- ✅ maven-compiler-plugin: source/target/release = 21
- ✅ Dockerfile: eclipse-temurin-21

### 2. Microservices
- ✅ Parent pom.xml: `java.version` = 21
- ✅ common-service/pom.xml: `java.version` = 21
- ✅ user-management-service/pom.xml: `java.version` = 21
- ✅ api-gateway/Dockerfile: eclipse-temurin-21
- ✅ All service Dockerfiles: eclipse-temurin-21

## Yêu cầu hệ thống

### Development
```bash
# Kiểm tra Java version
java -version

# Nên thấy output:
# openjdk version "21.x.x"
# hoặc
# java version "21.x.x"
```

### Download JDK 21

**Option 1: Eclipse Temurin (Recommended)**
- URL: https://adoptium.net/temurin/releases/?version=21
- Chọn: JDK 21 (LTS)
- Platform: Windows x64 / macOS / Linux

**Option 2: Oracle JDK**
- URL: https://www.oracle.com/java/technologies/downloads/#java21
- Chọn: Java SE Development Kit 21

**Option 3: Amazon Corretto**
- URL: https://aws.amazon.com/corretto/
- Chọn: Amazon Corretto 21

### Cài đặt

**Windows:**
```bash
# Download installer từ link trên
# Chạy installer
# Set JAVA_HOME environment variable
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-21.x.x"
setx PATH "%PATH%;%JAVA_HOME%\bin"
```

**macOS (Homebrew):**
```bash
brew install openjdk@21
sudo ln -sfn /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

## Build Instructions

### Clean và rebuild

```bash
# Backend monolithic
cd backend
mvn clean install

# Microservices
cd microservices

# Build common service first
cd common-service
mvn clean install

# Build parent
cd ..
mvn clean install
```

### Nếu vẫn gặp lỗi

```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Rebuild
mvn clean install -U
```

## Docker Build

Dockerfiles đã được update để sử dụng JDK 21:

```dockerfile
# Build stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
```

## Tính năng Java 21

Dù downgrade từ JDK 25, JDK 21 vẫn có nhiều tính năng mới:

### 1. Virtual Threads (Preview)
```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> {
        // Task code
    });
}
```

### 2. Pattern Matching for switch
```java
String result = switch (obj) {
    case String s -> "String: " + s;
    case Integer i -> "Integer: " + i;
    default -> "Unknown";
};
```

### 3. Record Patterns
```java
record Point(int x, int y) {}

if (obj instanceof Point(int x, int y)) {
    System.out.println("x: " + x + ", y: " + y);
}
```

### 4. Sequenced Collections
```java
List<String> list = new ArrayList<>();
list.addFirst("first");
list.addLast("last");
String first = list.getFirst();
String last = list.getLast();
```

## Compatibility Matrix

| Component | JDK 21 | JDK 25 |
|-----------|--------|--------|
| Spring Boot 3.4.1 | ✅ Full | ⚠️ Partial |
| Lombok 1.18.36 | ✅ Full | ❌ Limited |
| MapStruct 1.6.3 | ✅ Full | ⚠️ Partial |
| PostgreSQL Driver | ✅ Full | ✅ Full |
| Flyway | ✅ Full | ✅ Full |
| JWT (jjwt 0.12.6) | ✅ Full | ✅ Full |

## Migration Path (Future)

Khi muốn nâng cấp lên JDK 25 trong tương lai:

1. Đợi Lombok release version hỗ trợ JDK 25
2. Đợi Spring Boot release version hỗ trợ đầy đủ JDK 25
3. Test kỹ tất cả dependencies
4. Update pom.xml files
5. Update Dockerfiles
6. Rebuild và test

## Troubleshooting

### Lỗi: "Unsupported class file major version"
**Giải pháp**: Đảm bảo đang dùng JDK 21, không phải JDK 17 hoặc cũ hơn

### Lỗi: "Fatal error compiling: java.lang.ExceptionInInitializerError"
**Giải pháp**: 
1. Clean Maven cache: `rm -rf ~/.m2/repository`
2. Verify Java version: `java -version`
3. Rebuild: `mvn clean install -U`

### Lỗi: "release version 21 not supported"
**Giải pháp**: Update Maven Compiler Plugin lên 3.14.0

## Kết luận

JDK 21 là lựa chọn tốt nhất hiện tại cho production:
- ✅ Stable và reliable
- ✅ Long Term Support
- ✅ Excellent library compatibility
- ✅ Modern Java features
- ✅ Performance improvements

Dự án sẽ hoạt động tốt với JDK 21 và có thể nâng cấp lên JDK 25 trong tương lai khi ecosystem đã sẵn sàng.

---

**Updated**: 2024
**Java Version**: 21 (LTS)
**Spring Boot**: 3.4.1
**Lombok**: 1.18.36
