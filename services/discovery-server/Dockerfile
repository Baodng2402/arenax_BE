FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace/app

# Copy gradle wrapper and build files first for dependency caching
COPY gradlew settings.gradle.kts build.gradle.kts gradle/ ./
COPY build-logic/ build-logic/
COPY services/discovery-server/build.gradle.kts services/discovery-server/

# Download dependencies (cached if build files don't change)
RUN ./gradlew :services:discovery-server:dependencies --no-daemon

# Copy source code and build jar
COPY services/discovery-server/src/ services/discovery-server/src/
RUN ./gradlew :services:discovery-server:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/app/services/discovery-server/build/libs/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
