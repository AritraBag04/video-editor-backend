# Use Eclipse Temurin JDK 21 as base image
FROM eclipse-temurin:21-jdk

# Install FFmpeg
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY target/*.jar app.jar

# Set entrypoint
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]