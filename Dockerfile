# ==============================================================================
# Build Stage
# ==============================================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml first to cache the dependency downloads
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the application files and build the artifact
COPY src ./src
RUN mvn clean package -DskipTests

# ==============================================================================
# Run Stage
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the compiled jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
