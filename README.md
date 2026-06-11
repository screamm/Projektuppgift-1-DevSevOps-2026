# Todo DevSecOps

A fullstack todo application built for a DevSecOps course project, focused on continuous development and automated testing.

The app provides user accounts (registration and sign-in, with passwords hashed using BCrypt) and task management on a dashboard: list tasks, create new tasks, mark tasks as completed, and delete tasks with a confirmation step. A settings page at `/settings` lets a signed-in user update their username, change their password, and edit a personal task list that is seeded for each user at registration. The frontend never talks to the backend directly from the browser — all API calls go through Next.js route handlers (`app/api/*`) that proxy requests to the Spring Boot backend.

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | Next.js 16 (App Router), React 19, TypeScript, Tailwind CSS 4 |
| Backend | Java 17, Spring Boot 3.3 (REST API on port 8080) |
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

Registers a new user, stores the password as a BCrypt hash, and seeds the user's personal task list (see [User profile & per-user tasks](#user-profile--per-user-tasks)). Returns `200` in both cases; `exists` indicates whether the email was already registered.

| | |
|---|---|
| Method | `POST` |
| Path | `/api/users` |
| Request body | `{"username": string, "email": string, "password": string}` |
| Response | `200 OK` — `{"exists": boolean, "message": string}` |

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username": "david", "email": "david@example.com", "password": "secret123"}'
```

### POST /api/auth/login

Validates credentials. Returns `200` in both cases; `success` indicates whether the login succeeded.

| | |
|---|---|
| Method | `POST` |
| Path | `/api/auth/login` |
| Request body | `{"email": string, "password": string}` |
| Response | `200 OK` — `{"success": boolean, "message": string}` |

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "david@example.com", "password": "secret123"}'
```

### User profile & per-user tasks

These endpoints back the settings page (`/settings`). Unlike `/api/tasks`, which uses semantic status codes, they always respond `200 OK` and signal the outcome with a `success` flag in the response body (same convention as `POST /api/users` and `POST /api/auth/login` above).

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

#### GET /api/users/{email}

Returns the user's profile.

| | |
|---|---|
| Method | `GET` |
| Path | `/api/users/{email}` |
| Request body | — |
| Response | `200 OK` — `{"success": boolean, "message": string, "username": string\|null, "email": string\|null}` |

`success` is `false` (with `username` and `email` as `null`) when the email is not registered.

```bash
curl http://localhost:8080/api/users/david@example.com
```

#### PATCH /api/users/{email}

Updates the user's username.

| | |
|---|---|
| Method | `PATCH` |
| Path | `/api/users/{email}` |
| Request body | `{"username": string}` |
| Response | `200 OK` — `{"success": boolean, "message": string, "username": string\|null, "email": string\|null}` |

`success` is `false` when the email is not registered or `username` is missing/empty.

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
| Response | `200 OK` — `{"success": boolean, "message": string}` |

`success` is `false` when the email is not registered, `currentPassword` is incorrect, or `newPassword` is shorter than 8 characters.

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
| Response | `200 OK` — array of per-user Task objects (empty array for unknown emails) |

```bash
curl http://localhost:8080/api/users/david@example.com/tasks
```

#### PUT /api/users/{email}/tasks/{taskId}

| | |
|---|---|
| Method | `PUT` |
| Path | `/api/users/{email}/tasks/{taskId}` |
| Request body | `{"title": string, "priority": "High"\|"Medium"\|"Low", "completed": boolean}` |
| Response | `200 OK` — the updated per-user Task, or an empty body when the email or task id is unknown or `title` is missing/empty |

```bash
curl -X PUT http://localhost:8080/api/users/david@example.com/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Plan the next sprint", "priority": "High", "completed": true}'
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
| `e2e-tests` | Downloads and starts the backend jar, builds the production frontend, and runs Playwright (Chromium) against it (`npm run test:e2e`). Uploads the Playwright report on failure. Depends on `backend-tests` |
| `dependency-check` | OWASP Dependency-Check on the backend dependencies, with SARIF upload to GitHub Code Scanning and an HTML report artifact. Currently non-blocking (`continue-on-error: true`) until the `NVD_API_KEY` repository secret is configured, since NVD downloads are slow and unstable without an API key |

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
