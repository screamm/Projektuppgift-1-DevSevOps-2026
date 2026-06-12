# Todo DevSecOps

A fullstack todo application built for a DevSecOps course project, focused on continuous development and automated testing.

The app provides user accounts (registration and sign-in, with passwords hashed using BCrypt) and task management on a dashboard: list tasks, create new tasks, mark tasks as completed, and delete tasks with a confirmation step. A settings page at `/settings` lets a signed-in user update their username, change their password, and edit a personal task list that is seeded for each user at registration. The frontend never talks to the backend directly from the browser — all API calls go through Next.js route handlers (`app/api/*`) that proxy requests to the Spring Boot backend.

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | Next.js 16 (App Router), React 19, TypeScript, Tailwind CSS 4 |
| Backend | Java 17, Spring Boot 3.5 (REST API on port 8080) |
| Data storage | In-memory with seed data (resets on restart) |
| Unit tests | JUnit 5 via Spring Boot Test (Maven Surefire) |
| API tests | Postman collection run with Newman |
| E2E tests | Playwright (Chromium, desktop + mobile viewports) |
| CI | GitHub Actions |
| Security scanning | `npm audit`, OWASP Dependency-Check |

## Requirements

- JDK 17 or later
- Node.js 20 or later (with npm)

Maven does **not** need to be installed — the project ships with the Maven Wrapper (`Backend/mvnw`).

## Getting started

### 1. Start the backend (port 8080)

```bash
./Backend/mvnw -f Backend/pom.xml spring-boot:run
```

or equivalently:

```bash
cd Backend && ./mvnw spring-boot:run
```

### 2. Start the frontend (port 3000)

```bash
npm ci
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

The frontend reaches the backend through the `BACKEND_URL` environment variable, which defaults to `http://localhost:8080`. Set it before starting the frontend if the backend runs elsewhere:

```bash
BACKEND_URL=http://localhost:8080 npm run dev
```

### Production build

```bash
npm run build
npm run start
```

## Running tests

| Test type | Command | Notes |
|---|---|---|
| Backend unit tests | `./Backend/mvnw -f Backend/pom.xml test` | No services need to be running |
| Lint | `npm run lint` | ESLint |
| API tests (Newman) | `npm run test:api` | Requires a running backend on port 8080. Collection lives in `tests/api/` |
| E2E tests (Playwright) | `npm run test:e2e` | See prerequisites below |

### E2E prerequisites

The E2E suite runs against the **production build** of the frontend and the packaged backend jar. `playwright.config.ts` starts both servers automatically, but you must first:

```bash
# 1. Build the backend jar
./Backend/mvnw -f Backend/pom.xml -DskipTests package

# 2. Build the production frontend
npm run build

# 3. Install the Playwright browser (first time only)
npx playwright install chromium

# 4. Run the tests
npm run test:e2e
```

## API documentation

Base URL: `http://localhost:8080` (the frontend proxies the same paths under `http://localhost:3000/api/*`).

### Task model

| Field | Type | Notes |
|---|---|---|
| `id` | number | Assigned by the server |
| `title` | string | Required, non-empty, max 100 characters |
| `priority` | string | Required, one of `LOW`, `MEDIUM`, `HIGH` |
| `dueDate` | string or null | Optional, format `YYYY-MM-DD` |
| `completed` | boolean | Defaults to `false` |

Seed data (loaded at startup):

| Title | Priority | Due date |
|---|---|---|
| Plan the next sprint | HIGH | 2026-06-15 |
| Review security checklist | MEDIUM | 2026-06-18 |
| Update project documentation | LOW | — |

### GET /api/tasks

| | |
|---|---|
| Method | `GET` |
| Path | `/api/tasks` |
| Request body | — |
| Response | `200 OK` — array of Task objects |

```bash
curl http://localhost:8080/api/tasks
```

### GET /api/tasks/{id}

| | |
|---|---|
| Method | `GET` |
| Path | `/api/tasks/{id}` |
| Request body | — |
| Response | `200 OK` — Task object |
| Errors | `404 Not Found` — `{"message": "..."}` |

```bash
curl http://localhost:8080/api/tasks/1
```

### POST /api/tasks

| | |
|---|---|
| Method | `POST` |
| Path | `/api/tasks` |
| Request body | `{"title": string, "priority": "LOW"\|"MEDIUM"\|"HIGH", "dueDate"?: "YYYY-MM-DD", "completed"?: boolean}` |
| Response | `201 Created` — the created Task |
| Errors | `400 Bad Request` — `{"message": "...", "errors": {...}}` on validation failure |

`title` is required (non-empty, max 100 characters) and `priority` is required.

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Write project report", "priority": "HIGH", "dueDate": "2026-06-20"}'
```

### PUT /api/tasks/{id}

| | |
|---|---|
| Method | `PUT` |
| Path | `/api/tasks/{id}` |
| Request body | `{"title": string, "priority": "LOW"\|"MEDIUM"\|"HIGH", "dueDate"?: "YYYY-MM-DD", "completed": boolean}` |
| Response | `200 OK` — the updated Task |
| Errors | `400 Bad Request` — validation failure; `404 Not Found` — unknown id |

```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Plan the next sprint", "priority": "HIGH", "dueDate": "2026-06-15", "completed": true}'
```

### DELETE /api/tasks/{id}

| | |
|---|---|
| Method | `DELETE` |
| Path | `/api/tasks/{id}` |
| Request body | — |
| Response | `204 No Content` |
| Errors | `404 Not Found` — unknown id |

```bash
curl -i -X DELETE http://localhost:8080/api/tasks/1
```

### POST /api/users

Registers a new user, stores the password as a BCrypt hash, and seeds the user's personal task list (see [User profile & per-user tasks](#user-profile--per-user-tasks)). Returns `201 Created` on success, `409 Conflict` when the email is already registered (`exists` is `true`), and `400 Bad Request` on validation failure (missing username, missing or invalid email, or a password shorter than 8 characters).

| | |
|---|---|
| Method | `POST` |
| Path | `/api/users` |
| Request body | `{"username": string, "email": string, "password": string}` |
| Response | `201 Created` — `{"exists": false, "message": "User created"}` |
| Errors | `409 Conflict` — email already registered; `400 Bad Request` — validation failure (both with the same `{"exists": boolean, "message": string}` body) |

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username": "david", "email": "david@example.com", "password": "secret123"}'
```

### POST /api/auth/login

Validates credentials. Returns `200 OK` when the login succeeds, `401 Unauthorized` when the email or password is wrong, and `400 Bad Request` when the email is missing or malformed.

| | |
|---|---|
| Method | `POST` |
| Path | `/api/auth/login` |
| Request body | `{"email": string, "password": string}` |
| Response | `200 OK` — `{"success": true, "message": "Login successful", "email": string}` |
| Errors | `401 Unauthorized` — wrong email or password; `400 Bad Request` — missing or malformed email (both with `{"success": false, "message": string, "email": null}`) |

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "david@example.com", "password": "secret123"}'
```

### POST /api/auth/register

Alternative registration endpoint with the same contract as `POST /api/users` (both delegate to the same user service).

| | |
|---|---|
| Method | `POST` |
| Path | `/api/auth/register` |
| Request body | `{"username": string, "email": string, "password": string}` |
| Response | `201 Created` — `{"exists": false, "message": "User created"}` |
| Errors | `409 Conflict` — email already registered; `400 Bad Request` — validation failure |

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "david", "email": "david2@example.com", "password": "secret123"}'
```

### User profile & per-user tasks

These endpoints back the settings page (`/settings`). They use semantic status codes (`404 Not Found` for unknown emails or task ids, `400 Bad Request` for validation failures) and additionally include a `success` flag in profile/password response bodies.

Each user gets their own task list, seeded at registration. The per-user task model differs from the shared Task model above:

| Field | Type | Notes |
|---|---|---|
| `id` | number | Assigned by the server |
| `title` | string | Required, non-empty |
| `priority` | string | One of `High`, `Medium`, `Low` — any other value is normalized to `Medium` |
| `completed` | boolean | |

Per-user seed data (created for each user by `POST /api/users`):

| Title | Priority |
|---|---|
| Plan the next sprint | High |
| Review security checklist | Medium |
| Update project documentation | Low |

#### GET /api/users/{email}/settings

Returns the user's profile. The frontend's `GET /api/users/{email}` route proxies to this endpoint.

| | |
|---|---|
| Method | `GET` |
| Path | `/api/users/{email}/settings` |
| Request body | — |
| Response | `200 OK` — `{"success": true, "message": "Profile loaded", "username": string, "email": string}` |
| Errors | `404 Not Found` — unknown email; `400 Bad Request` — missing or malformed email (both with `{"success": false, "message": string, "username": null, "email": null}`) |

```bash
curl http://localhost:8080/api/users/david@example.com/settings
```

#### PATCH /api/users/{email}

Updates the user's username.

| | |
|---|---|
| Method | `PATCH` |
| Path | `/api/users/{email}` |
| Request body | `{"username": string}` |
| Response | `200 OK` — `{"success": true, "message": "Username updated", "username": string, "email": string}` |
| Errors | `404 Not Found` — unknown email; `400 Bad Request` — `username` missing/empty (both with `success: false` and a `message`) |

```bash
curl -X PATCH http://localhost:8080/api/users/david@example.com \
  -H "Content-Type: application/json" \
  -d '{"username": "david_r"}'
```

#### PATCH /api/users/{email}/password

Changes the user's password. The new password is stored as a BCrypt hash.

| | |
|---|---|
| Method | `PATCH` |
| Path | `/api/users/{email}/password` |
| Request body | `{"currentPassword": string, "newPassword": string}` |
| Response | `200 OK` — `{"success": true, "message": "Password updated"}` |
| Errors | `404 Not Found` — unknown email; `400 Bad Request` — `currentPassword` incorrect or missing, or `newPassword` shorter than 8 characters (both with `success: false` and a `message`) |

```bash
curl -X PATCH http://localhost:8080/api/users/david@example.com/password \
  -H "Content-Type: application/json" \
  -d '{"currentPassword": "secret123", "newPassword": "longersecret456"}'
```

#### GET /api/users/{email}/tasks

| | |
|---|---|
| Method | `GET` |
| Path | `/api/users/{email}/tasks` |
| Request body | — |
| Response | `200 OK` — array of per-user Task objects |
| Errors | `404 Not Found` — unknown email; `400 Bad Request` — missing or malformed email (both with `{"message": string}`) |

```bash
curl http://localhost:8080/api/users/david@example.com/tasks
```

#### PUT /api/users/{email}/tasks/{taskId}

| | |
|---|---|
| Method | `PUT` |
| Path | `/api/users/{email}/tasks/{taskId}` |
| Request body | `{"title": string, "priority": "High"\|"Medium"\|"Low", "completed": boolean}` |
| Response | `200 OK` — the updated per-user Task |
| Errors | `404 Not Found` — unknown email or task id; `400 Bad Request` — `title` missing/empty or malformed email (both with `{"message": string}`) |

```bash
curl -X PUT http://localhost:8080/api/users/david@example.com/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Plan the next sprint", "priority": "High", "completed": true}'
```

### User administration

#### GET /api/users

| | |
|---|---|
| Method | `GET` |
| Path | `/api/users` |
| Request body | — |
| Response | `200 OK` — `{"users": [{"username": string, "email": string}, ...]}` |

```bash
curl http://localhost:8080/api/users
```

#### GET /api/users/{email}

| | |
|---|---|
| Method | `GET` |
| Path | `/api/users/{email}` |
| Request body | — |
| Response | `200 OK` — `{"username": string, "email": string}` |
| Errors | `404 Not Found` — unknown email; `400 Bad Request` — missing or malformed email (both with `{"message": string}`) |

```bash
curl http://localhost:8080/api/users/david@example.com
```

#### DELETE /api/users/{email}

Deletes the user and their personal task list.

| | |
|---|---|
| Method | `DELETE` |
| Path | `/api/users/{email}` |
| Request body | — |
| Response | `200 OK` — `{"success": true, "message": "User deleted"}` |
| Errors | `404 Not Found` — unknown email; `400 Bad Request` — missing or malformed email (both with `success: false` and a `message`) |

```bash
curl -X DELETE http://localhost:8080/api/users/david@example.com
```

#### PUT /api/users/{email}/password

Alternative password-change endpoint with the same rules as `PATCH /api/users/{email}/password`.

| | |
|---|---|
| Method | `PUT` |
| Path | `/api/users/{email}/password` |
| Request body | `{"currentPassword": string, "newPassword": string}` |
| Response | `200 OK` — `{"success": true, "message": "Password updated"}` |
| Errors | `404 Not Found` — unknown email; `400 Bad Request` — `currentPassword` incorrect or missing, or `newPassword` shorter than 8 characters |

```bash
curl -X PUT http://localhost:8080/api/users/david@example.com/password \
  -H "Content-Type: application/json" \
  -d '{"currentPassword": "secret123", "newPassword": "longersecret456"}'
```

### Profile

#### GET /api/profile

| | |
|---|---|
| Method | `GET` |
| Path | `/api/profile?email={email}` |
| Request body | — |
| Response | `200 OK` — `{"username": string, "email": string, "message": null}` |
| Errors | `404 Not Found` — unknown email; `400 Bad Request` — missing or malformed email (both with a `message`) |

```bash
curl "http://localhost:8080/api/profile?email=david@example.com"
```

#### PUT /api/profile

Updates username and/or email for the user identified by `currentEmail`. The personal task list follows the user to the new email.

| | |
|---|---|
| Method | `PUT` |
| Path | `/api/profile` |
| Request body | `{"currentEmail": string, "username": string, "email": string}` |
| Response | `200 OK` — `{"success": true, "message": "Profile updated", "email": string}` |
| Errors | `404 Not Found` — `currentEmail` not registered; `409 Conflict` — new email already in use; `400 Bad Request` — validation failure |

```bash
curl -X PUT http://localhost:8080/api/profile \
  -H "Content-Type: application/json" \
  -d '{"currentEmail": "david@example.com", "username": "david_r", "email": "david.r@example.com"}'
```

### Dashboard & health

#### GET /api/dashboard/summary

| | |
|---|---|
| Method | `GET` |
| Path | `/api/dashboard/summary?email={email}` |
| Request body | — |
| Response | `200 OK` — `{"username": string, "email": string, "accountStatus": "active", "userCount": number, "tasksToday": number, "completed": number, "remaining": number}` |
| Errors | `404 Not Found` — unknown email; `400 Bad Request` — missing or malformed email (both with a `message`) |

```bash
curl "http://localhost:8080/api/dashboard/summary?email=david@example.com"
```

#### GET /api/health

| | |
|---|---|
| Method | `GET` |
| Path | `/api/health` |
| Request body | — |
| Response | `200 OK` — `{"status": "ok", "message": "Backend is running"}` |

```bash
curl http://localhost:8080/api/health
```

## CI pipelines

Two GitHub Actions workflows live in [`.github/workflows/`](.github/workflows/).

### CI (`backendWorkflow.yml`)

The main pipeline with five jobs. It triggers on pushes to `main` and `dev`, on pull requests targeting `main` or `dev`, and manually via `workflow_dispatch` (feature branches are covered by their pull requests, avoiding duplicate runs).

| Job | What it does |
|---|---|
| `backend-tests` | Builds and unit-tests the backend with `./mvnw clean verify` (Java 17), verifies the build does not modify tracked files, and uploads the jar as an artifact |
| `frontend-checks` | `npm ci`, ESLint, a TypeScript check (`npx tsc --noEmit`), `npm audit --omit=dev --audit-level=high` (blocking gate on high/critical advisories; two known moderate advisories via `next`/`postcss` are documented in the workflow), and a production build |
| `api-tests` | Downloads the backend jar, starts it, waits for `/api/tasks` to respond, then runs the Newman collection (`npm run test:api`). Depends on `backend-tests` |
| `e2e-tests` | Downloads the backend jar, builds the production frontend, and runs Playwright (Chromium) via `npm run test:e2e` — `playwright.config.ts` starts both the backend jar and the frontend server. Uploads the Playwright report on failure. Depends on `backend-tests` |
| `dependency-check` | OWASP Dependency-Check on the backend dependencies, with SARIF upload to GitHub Code Scanning and an HTML report artifact. Blocking gate (`failBuildOnCVSS: 7`) using the `NVD_API_KEY` repository secret; the OSS Index analyzer is disabled since anonymous access now returns 401 |

### Frontend CI (`frontendWorkflow.yml`)

A lighter, path-filtered workflow that gives fast feedback on frontend-only changes. It triggers on pushes to `main`, `dev`, `frontend/**` and `feature/**` branches, on pull requests targeting `main` or `dev` (in both cases only when frontend files such as `app/**`, `lib/**` or the frontend configs change), and manually via `workflow_dispatch`. A single `test` job runs `npm ci`, ESLint, a TypeScript check (`npx tsc --noEmit`), and a production build on Node 20.

## Project structure

```
.
├── app/                     # Next.js frontend (App Router)
│   ├── (auth)/              # Sign-in and sign-up pages
│   ├── api/                 # Route handlers proxying the backend API
│   ├── dashboard/           # Task dashboard (list, create, complete, delete)
│   └── settings/            # Settings page (profile, password change, per-user tasks)
├── Backend/                 # Spring Boot REST API (port 8080)
│   ├── src/main/java/       # Controllers, services, models
│   └── src/test/java/       # Unit tests
├── lib/                     # Shared frontend utilities (backend proxy helper)
├── tests/
│   ├── api/                 # Postman/Newman collection
│   └── e2e/                 # Playwright E2E tests
├── .github/workflows/       # CI pipelines (backendWorkflow.yml, frontendWorkflow.yml)
└── playwright.config.ts     # E2E config — starts backend jar + production frontend
```
