# stage-1 : complete build environment
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
RUN addgroup demogroup; adduser --ingroup demogroup --disabled-password demo
USER demo
WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn clean install -Dmaven.test.skip=true


# stage-2 : minimal runtime environment
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# copy jar from the first stage
COPY --from=builder /app/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]