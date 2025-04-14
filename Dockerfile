
# Start with a Maven image to build the application
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY FInalYearProject/pom.xml .

# Download dependencies (improves caching)
RUN mvn dependency:go-offline

# Copy the source code
COPY FInalYearProject /app

# Package the application
RUN mvn clean package -DskipTests

# Use a minimal runtime image
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]

#Place this Dockerfile in the root of your project directory.
#
#Run the following commands to build and run the Docker image:
#docker build -t final-year-project .
#docker run -p 8080:8080 final-year-project
