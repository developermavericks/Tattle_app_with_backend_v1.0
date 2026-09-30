# Multi-stage Dockerfile for Tattle Ktor Netty Backend
FROM gradle:8.5-jdk21-alpine AS build
WORKDIR /app
COPY . .
RUN ./gradlew :server:build --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/server/build/libs/*.jar app.jar
EXPOSE 8081
HEALTHCHECK --interval=15s --timeout=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8081/ || exit 1
CMD ["java", "-jar", "app.jar"]
