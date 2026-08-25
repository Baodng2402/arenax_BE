FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace/app

# Copy gradle wrapper and build files first for dependency caching
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle/ gradle/
COPY build-logic/ build-logic/

# settings.gradle.kts includes every module below, so Gradle's configuration
# phase needs each one's build.gradle.kts on disk even though we only build
# discovery-server here.
COPY libs/messaging-foundation/build.gradle.kts libs/messaging-foundation/
COPY libs/trusted-gateway-security/build.gradle.kts libs/trusted-gateway-security/
COPY services/api-gateway/build.gradle.kts services/api-gateway/
COPY services/identity-service/build.gradle.kts services/identity-service/
COPY services/tenant-service/build.gradle.kts services/tenant-service/
COPY services/subscription-service/build.gradle.kts services/subscription-service/
COPY services/competition-service/build.gradle.kts services/competition-service/
COPY services/ranking-service/build.gradle.kts services/ranking-service/
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
