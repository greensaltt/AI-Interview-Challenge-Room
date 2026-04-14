# AI Interview Battle Room

This repository hosts the MVP scaffold for the AI interview training platform described in `memory-bank/`.

Current implementation status:

- Step 1 is complete: backend/frontend project scaffold
- Step 2 is complete in code: local infrastructure runtime environment
- Step 3 and beyond are intentionally not started yet

## Project Structure

- `backend/`: Spring Boot backend application
- `frontend/`: Vue 3 frontend application
- `memory-bank/`: product, architecture, tech stack, and implementation documents
- `AGENTS.md`: collaboration rules for future AI developers

## Local Startup

### 1. Start local infrastructure

Requirements:

- Docker Desktop / Docker Engine
- Docker Compose v2

Commands:

```powershell
Copy-Item deploy/local/.env.example deploy/local/.env
docker compose --env-file deploy/local/.env -f deploy/local/docker-compose.yml up -d
```

Expected result:

- PostgreSQL starts on `localhost:5432`
- Redis starts on `localhost:6379`
- The initial database is created with `pgvector` enabled

### 2. Start backend

Requirements:

- Java 21
- Maven 3.9+

Commands:

```powershell
cd backend
mvn spring-boot:run
```

Expected result:

- The service starts on `http://localhost:8080`
- Health check is available at `http://localhost:8080/api/health`
- Dependency probe is available at `http://localhost:8080/api/health/dependencies`
- Async task polling contract is reserved at `http://localhost:8080/api/tasks/{taskId}`

Notes:

- The backend defaults to the `local` profile.
- Local profile reads PostgreSQL and Redis settings from `AI_INTERVIEW_*` environment variables, with localhost defaults matching `deploy/local/docker-compose.yml`.
- Test profile disables external PostgreSQL/Redis autoconfiguration so backend tests can run without local infrastructure.
- Production profile requires explicit `AI_INTERVIEW_*` environment variables and does not expose health details.

### 3. Start frontend

Requirements:

- Node.js 22+
- npm 10+

Commands:

```powershell
cd frontend
npm install
npm run dev
```

Expected result:

- The app starts on the Vite dev server
- The default homepage is available in the browser
- Vite proxies `/api` and `/actuator` to the backend target configured by `VITE_DEV_PROXY_TARGET`

Environment files:

- `deploy/local/.env.example`: shared local infrastructure defaults
- `frontend/.env.development.example`: frontend development environment template
- `frontend/.env.production.example`: frontend production environment template

Verification checklist for Step 2:

1. `docker compose --env-file deploy/local/.env -f deploy/local/docker-compose.yml ps`
2. `GET http://localhost:8080/api/health`
3. `GET http://localhost:8080/api/health/dependencies`
4. `GET http://localhost:8080/api/tasks/demo-task-id`

Notes:

- Step 3 database migration baseline has not been started yet.
- Business schema, Flyway migration scripts, and auth implementation are intentionally deferred until Step 2 is verified.

