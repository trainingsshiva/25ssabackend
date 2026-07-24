# Build Stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first for caching optimization
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests

# Run Stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/management-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV UPLOAD_DIR=/app/uploads
VOLUME /app/uploads
ENTRYPOINT ["java", "-jar", "app.jar"]
