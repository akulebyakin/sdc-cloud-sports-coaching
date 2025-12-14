FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

# Copy maven wrapper and pom files
COPY ../mvnw ../mvnw
COPY ../.mvn ../.mvn
COPY ../pom.xml ../pom.xml
COPY ../common ../common
COPY pom.xml .
COPY src src

# Build the application
RUN cd .. && chmod +x mvnw && ./mvnw clean package -pl common,review-service -am -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar
COPY --from=build /workspace/app/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8082/health || exit 1

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
