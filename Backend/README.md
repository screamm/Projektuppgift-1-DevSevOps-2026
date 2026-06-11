# Todo DevSecOps — Backend

Java 17 / Spring Boot REST API for the Todo DevSecOps application, served on port 8080. It exposes two task APIs: the shared task endpoints (`/api/tasks`, package `se.devsecops.backend.tasks`, semantic status codes) used by the dashboard, and the per-user task endpoints (`/api/users/{email}/tasks`, always `200 OK` with a `success`/body convention) used by the settings page and seeded for each user at registration. It also serves the user, profile and auth endpoints (`/api/users`, `/api/users/{email}`, `/api/users/{email}/password`, `/api/auth/login`) with BCrypt password hashing. Data is stored in memory with seed data and resets on restart.

Maven does not need to be installed — use the bundled Maven Wrapper (`./mvnw`).

## Run

```bash
./mvnw spring-boot:run
```

The API is then available at `http://localhost:8080`.

## Test

```bash
./mvnw test
```

Runs the 63 JUnit unit tests: 22 in the `tasks` package (shared task API) and 41 covering users, auth, profile, dashboard, health and per-user tasks.

## Package (jar)

Required before running the E2E tests from the repository root:

```bash
./mvnw -DskipTests package
```

## API documentation

See the [root README](../README.md#api-documentation) for the full endpoint reference, request/response formats, status codes and curl examples.
