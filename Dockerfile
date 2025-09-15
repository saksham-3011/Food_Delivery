# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set an argument for the JAR file
ARG JAR_FILE=target/*.jar

# Set the working directory in the container
WORKDIR /app

# Copy the Maven wrapper and pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# ---- FIX: Make the Maven wrapper executable ----
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy the project source
COPY src ./src

# Package the application
RUN ./mvnw package -DskipTests

# Copy the JAR file to the app directory
COPY ${JAR_FILE} app.jar

# Set the entrypoint to run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]