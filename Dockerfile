# Stage 1: Build the application
FROM maven:3.8.1-openjdk-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Add build arguments for DB configuration
ARG DB_URL
ARG DB_USERNAME
ARG DB_PASSWORD

# Copy the pom.xml and any other files necessary for dependencies
COPY pom.xml .
COPY src ./src

# Package the application (skip tests if desired)
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Add a volume pointing to /tmp
VOLUME /tmp

# Expose port 8080
EXPOSE 8080

# Copy the packaged application from the build stage
COPY --from=build /app/target/filethreader-0.0.1-SNAPSHOT.jar app.jar

# Pass environment variables to the container at runtime
ENTRYPOINT ["java", "-jar", "app.jar"]
