FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copy parent pom and common service
COPY pom.xml ./pom.xml
COPY common-service ./common-service

# Build common service first
WORKDIR /build/common-service
RUN mvn clean install -DskipTests

# Copy user service
WORKDIR /build/user-management-service
COPY user-management-service/pom.xml ./pom.xml
COPY user-management-service/src ./src

# Build user service
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/user-management-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
