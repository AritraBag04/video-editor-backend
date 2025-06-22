# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy the pom.xml and download dependencies (this step is cached)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the project and build it
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application using a lightweight JRE image
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port (you can change this if needed)
EXPOSE 8084

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
