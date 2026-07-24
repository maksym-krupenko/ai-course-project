FROM gradle:8.10.2-jdk21-alpine AS build
WORKDIR /workspace
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY src ./src
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/build/libs/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
