# Infrastructure & Compose Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enhance root `compose.yaml` with Redis and Eureka (`discovery-server`), add an optimized Dockerfile for `discovery-server`, and configure `spring-boot-docker-compose` globally in root `build.gradle.kts` so any service bootRun automatically detects running infrastructure or starts it if needed.

**Architecture:** 
- Add `redis` and `discovery-server` containers to `compose.yaml` with healthchecks.
- Create an optimized multi-stage Dockerfile for `discovery-server` that leverages Gradle caching (copying Gradle wrapper and build files first) to avoid heavy rebuilds when source code hasn't changed.
- Enable `spring-boot-docker-compose` in `subprojects` block of root `build.gradle.kts`. When containers are already running, Spring Boot reuses them without restarting.

**Tech Stack:** Docker, Docker Compose, Spring Boot 3.x, Spring Cloud Netflix Eureka, Gradle.

## Global Constraints
- Ensure healthchecks are correctly defined so dependent services can wait for healthy containers.
- Keep container builds optimized with proper layer caching.

---

### Task 1: Create Optimized Dockerfile for Discovery Server

**Files:**
- Create: `services/discovery-server/Dockerfile`

- [ ] **Step 1: Write cached multi-stage Dockerfile for discovery-server**

```dockerfile
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
```

- [ ] **Step 2: Commit Dockerfile**

```bash
git add services/discovery-server/Dockerfile
git commit -m "feat(discovery): add optimized Dockerfile for discovery-server"
```

---

### Task 2: Update Root `compose.yaml` with Redis and Discovery Server

**Files:**
- Modify: `compose.yaml`

- [ ] **Step 1: Update compose.yaml with healthchecks**

```yaml
services:
  postgres:
    image: postgres:16
    container_name: arenax-postgres
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: 12345
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./docker/postgres/init-databases.sql:/docker-entrypoint-initdb.d/init-databases.sql:ro
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 10

  redis:
    image: redis:7-alpine
    container_name: arenax-redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 3s
      retries: 5

  discovery-server:
    build:
      context: .
      dockerfile: services/discovery-server/Dockerfile
    container_name: arenax-discovery-server
    ports:
      - "8761:8761"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 20s

volumes:
  postgres-data:
  redis-data:
```

- [ ] **Step 2: Commit compose.yaml updates**

```bash
git add compose.yaml
git commit -m "infra: add redis and discovery-server to compose.yaml"
```

---

### Task 3: Configure Spring Boot Docker Compose in Root `build.gradle.kts`

**Files:**
- Modify: `build.gradle.kts` (Root)

- [ ] **Step 1: Add developmentOnly dependency to subprojects**

```kotlin
subprojects {
    // ... existing configuration ...

    dependencies {
        // ... existing dependencies ...
        developmentOnly(libs.spring.boot.docker.compose)
    }
}
```

- [ ] **Step 2: Commit gradle configuration**

```bash
git add build.gradle.kts
git commit -m "build: configure spring-boot-docker-compose across all subprojects"
```
