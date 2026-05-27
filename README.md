# ArenaX Backend

Spring Boot backend for ArenaX. The project uses Java 21, Gradle Kotlin DSL, PostgreSQL, Flyway, Spring Security JWT, Spring Data JPA, QueryDSL, MapStruct, Lombok, and Spotless.

## Tech Stack

- Java 21
- Spring Boot 4.x
- Gradle Kotlin DSL
- PostgreSQL 16
- Flyway migrations
- Spring Data JPA + QueryDSL
- Spring Security + JWT
- MapStruct + Lombok
- Springdoc OpenAPI
- Spotless + Google Java Format, 2-space indentation

## Local Setup

Requirements:

- JDK 21
- Docker / Docker Compose

Create a local env file if you want to override profile defaults:

```bash
cp .env.example .env
```

Gradle does not load `.env` automatically. To run with values from `.env`, export them first:

```bash
set -a; source .env; set +a
```

Start the database:

```bash
docker compose up -d
```

Run the application with the default `local` profile:

```bash
./gradlew bootRun
```

Build / verify:

```bash
./gradlew spotlessCheck compileJava test
```

Auto-format code:

```bash
./gradlew spotlessApply
```

Local database defaults:

- Database: `jdbc:postgresql://localhost:5432/arenax`
- Username: `arenax`
- Password: `arenax_dev_password`
- Flyway location: `classpath:db/migration`

## Application Profiles

Spring profile files are split under `src/main/resources`:

```text
application.yaml          # Common config and default profile
application-local.yaml    # Local Docker Compose database
application-dev.yaml      # Dev environment, env vars with local fallbacks
application-prod.yaml     # Production environment, required env vars
application-test.yaml     # Test profile using H2
```

`application.yaml` sets `spring.profiles.default=local`, so `./gradlew bootRun` runs local config unless another profile is specified.

### Run With A Profile

Local profile:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

Dev profile:

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

Prod profile:

```bash
SPRING_PROFILES_ACTIVE=prod \
DATABASE_URL='jdbc:postgresql://db-host:5432/arenax' \
DATABASE_USERNAME='arenax' \
DATABASE_PASSWORD='change-me' \
JWT_SECRET='replace-with-a-long-secret' \
./gradlew bootRun
```

Test profile:

```bash
SPRING_PROFILES_ACTIVE=test ./gradlew test
```

### Environment File

`.env.example` documents the environment variables used by the application. Copy it to `.env` for local overrides:

```bash
cp .env.example .env
```

Keep `.env` private. It is ignored by git. Only `.env.example` should be committed.

### Environment Variables

Common optional variables:

| Variable | Default | Purpose |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | HTTP server port |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Comma-separated allowed origins |
| `JWT_ACCESS_TOKEN_EXPIRATION_MS` | `900000` | Access token lifetime |
| `JWT_REFRESH_TOKEN_EXPIRATION_MS` | `604800000` | Refresh token lifetime |

Dev/prod database variables:

| Variable | Required in prod | Purpose |
| --- | --- | --- |
| `DATABASE_URL` | Yes | JDBC URL |
| `DATABASE_USERNAME` | Yes | Database username |
| `DATABASE_PASSWORD` | Yes | Database password |
| `JWT_SECRET` | Yes | JWT signing secret |
| `JPA_SHOW_SQL` | No | Show SQL in dev profile |

Production profile intentionally has no fallback for database credentials or `JWT_SECRET`; missing values should fail fast.

## Project Structure

Keep the structure simple and consistent with the current layout:

```text
src/main/java/com/bk/arenax
├── adapter
│   ├── repository      # Spring Data repositories and repository adapters
│   ├── rest            # REST controllers
│   └── service         # Service implementations / use cases
├── domain              # Entities, enums, domain model
├── dto                 # Request / response DTOs
├── infrastructure      # Security, exception handling, config, converters
├── port                # Service and repository interfaces
└── shared              # Shared utilities: pagination, constants
```

Package conventions:

- `adapter/rest`: receives requests, validates input, calls services, returns responses.
- `adapter/service`: handles business flows, transaction boundaries, and DTO mapping.
- `adapter/repository`: contains Spring Data JPA repositories and implementations of repository ports.
- `port/service`: service interfaces used by controllers.
- `port/repository`: repository interfaces used by services.
- `domain`: entities, enums, and domain objects. Avoid dependencies back to controllers or DTOs.
- `infrastructure`: technical configuration, security, exception handling, QueryDSL factory, converters.
- `shared`: common helpers without module-specific business logic.

## API Response Convention

The common response wrapper is `ApiResponse<T>`:

```json
{
  "data": {},
  "message": "Success"
}
```

Error response:

```json
{
  "message": "User not found",
  "exceptionCode": "ARENAX.USR.001"
}
```

Null fields are omitted because `ApiResponse` uses `@JsonInclude(NON_NULL)`.

## Error Convention

Error codes are defined in `infrastructure/exception/ErrorCode.java`.

Code format:

```text
ARENAX.<DOMAIN>.<NUMBER>
```

Examples:

- `ARENAX.AUTH.001`: authentication error
- `ARENAX.USR.001`: user error
- `ARENAX.VAL.001`: validation error
- `ARENAX.SYS.999`: unexpected internal error

When adding a new error:

1. Add an enum value to `ErrorCode` with `code`, `message`, and `HttpStatus`.
2. Throw exceptions with `ErrorCode`, for example `new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND)`.
3. Do not hardcode exception codes in services or controllers.

## Pagination Convention

Pagination is implemented in `shared/pagination`.

Request params:

```http
GET /api/v1/users?currentPage=1&pageSize=10
```

Do not add sort/search/filter by default. The supported fields are only:

- `currentPage`: 1-based, default `1`
- `pageSize`: default `10`, max `500`

Response shape:

```json
{
  "data": [],
  "message": "Success",
  "pagination": {
    "page": 1,
    "size": 10,
    "numberOfElements": 0,
    "totalElements": 0,
    "totalPages": 0,
    "first": true,
    "last": true,
    "empty": true
  }
}
```

When adding a new list API:

1. Let the controller receive `@ModelAttribute BasePaginationRequest request`.
2. Return `BasePaginationResponse<T>` from the service.
3. Use `PaginationHelper.setPage(request)` in the repository to create `Pageable`.
4. Do not add sort/search/filter unless the endpoint explicitly needs it.

## Database Convention

Migrations live in:

```text
src/main/resources/db/migration
```

Flyway file naming:

```text
V<version>__<description>.sql
```

Example:

```text
V4__add_match_tables.sql
```

Rules:

- Do not edit migrations that have already been applied or shared.
- Make schema changes through new migration files.
- Entities must match the schema because `ddl-auto` is set to `validate`.
- Keep profile-specific database settings in `application-<profile>.yaml`, not in Java code.

## QueryDSL Convention

QueryDSL is configured through Gradle annotation processors and `JPAQueryFactory` at:

```text
infrastructure/config/QuerydslConfig.java
```

Generated Q classes are placed under `build/generated/...` and must not be committed.

Use QueryDSL when a query becomes more complex than a simple Spring Data derived query. For simple queries, prefer Spring Data JPA methods.

## Formatting Convention

Spotless is the required formatter.

Before committing, run:

```bash
./gradlew spotlessApply
./gradlew spotlessCheck compileJava test
```

Java formatting uses Google Java Format default style with 2-space indentation.

Basic rules:

- Do not manually format code against Spotless output.
- Do not commit generated files under `build/`.
- Do not leave unused imports.
- End files with a newline.

## Coding Convention

General:

- Use constructor injection with Lombok `@RequiredArgsConstructor`.
- Use `record` for simple request/response DTOs; use classes when builder/inheritance is needed.
- Validate requests with Jakarta Validation annotations in DTOs.
- Put `@Transactional` on service methods that change data.
- Do not put business logic in controllers.

Naming:

- Controller: `*Controller`
- Service interface: `*Service`
- Service implementation: `*ServiceImpl`
- Repository port: `*Repository`
- Spring Data repository: `Jpa*Repository`, or an existing explicit name such as `RefreshTokenRepository`
- Exception: `*Exception`
- Request DTO: `*Request`
- Response DTO: `*Response`

## Common Commands

```bash
# Start local Postgres
docker compose up -d

# Stop local Postgres
docker compose down

# Run app with default local profile
./gradlew bootRun

# Run app with explicit profile
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun

# Format code
./gradlew spotlessApply

# Check formatting
./gradlew spotlessCheck

# Compile
./gradlew compileJava

# Test
./gradlew test

# Full local verification
./gradlew spotlessCheck compileJava test
```
