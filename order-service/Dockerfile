# Multi-stage Dockerfile for Spring service
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -B -q dependency:go-offline
COPY src ./src
RUN ./mvnw -B -q package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java -jar /app/app.jar"]
