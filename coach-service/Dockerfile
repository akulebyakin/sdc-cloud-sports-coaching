FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace/app

# Copy maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY common common
COPY coach-service coach-service
COPY session-service session-service
COPY review-service review-service

# Build the application
RUN chmod +x mvnw && ./mvnw clean package -pl common,coach-service -am -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar
COPY --from=build /workspace/app/coach-service/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/health || exit 1

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
