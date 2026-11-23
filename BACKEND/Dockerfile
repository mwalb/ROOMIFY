# Stage 1: Build using official Maven on Java 21 (no local wrapper required)
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and package JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Java 21 runtime container
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the compiled Spring Boot executable JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
