FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY accounting-service/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
