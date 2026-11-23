# Use Java 21 JDK
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy the entire BACKEND directory
COPY BACKEND/ .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Build the Spring Boot application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Run JAR file
CMD ["java", "-jar", "target/Roomify-0.0.1-SNAPSHOT.jar"]
