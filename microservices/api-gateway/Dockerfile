FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copy parent pom and common service
COPY pom.xml ./pom.xml
COPY common-service ./common-service

# Build common service first
WORKDIR /build/common-service
RUN mvn clean install -DskipTests

# Copy api-gateway
WORKDIR /build
COPY api-gateway/pom.xml ./api-gateway/pom.xml
COPY api-gateway/src ./api-gateway/src

# Build api-gateway
WORKDIR /build/api-gateway
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
