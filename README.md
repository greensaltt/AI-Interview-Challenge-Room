# AI Interview Battle Room

This repository hosts the MVP scaffold for the AI interview training platform described in `memory-bank/`.

## Project Structure

- `backend/`: Spring Boot backend application
- `frontend/`: Vue 3 frontend application
- `memory-bank/`: product, architecture, tech stack, and implementation documents
- `AGENTS.md`: collaboration rules for future AI developers

## Local Startup

### Backend

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

### Frontend

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

## Notes

- Step 1 of `memory-bank/implementation-plan.md` is the only implemented step at this stage.
- Infrastructure, database, AI integration, and business modules are intentionally not started yet.

