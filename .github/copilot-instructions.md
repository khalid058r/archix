# Copilot Instructions for archix

## Project Overview
- **Backend**: Java (Spring Boot), see `Backend/`.
- **Frontend**: React + TypeScript + Vite, see `Frontend/`.
- Monorepo: Both apps are in the same root directory, but are built and run independently.

## Key Workflows
### Backend
- **Build**: Use `./mvnw clean install` from `Backend/`.
- **Run**: Use `./mvnw spring-boot:run` from `Backend/`.
- **Logs**: Build and compile logs are in `Backend/build_logs.txt`, `compile_log*.txt`, and `mvn_log.txt`.
- **Legacy code**: See `Backend/legacy_backup/` for old controllers, DTOs, entities, and configs. Do not update these unless migrating legacy logic.
- **Source**: Main code is in `Backend/src/main/`.
- **Tests**: Place tests in `Backend/src/test/`.
- **Project config**: See `Backend/pom.xml` for dependencies and build setup.

### Frontend
- **Install**: Run `npm install` in `Frontend/`.
- **Dev server**: Run `npm run dev` in `Frontend/`.
- **Build**: Run `npm run build` in `Frontend/`.
- **Source**: Main code is in `Frontend/src/`.
- **Config**: See `Frontend/vite.config.ts`, `tsconfig*.json`, and `package.json`.

## Conventions & Patterns
- **Backend**: Follows standard Spring Boot structure. DTOs, entities, controllers, mappers, and services are in their respective subfolders.
- **Frontend**: Uses React functional components, hooks, and context. TypeScript types are in `Frontend/src/types/`.
- **No custom AI agent rules or conventions found in the repo.**

## Integration Points
- No explicit cross-service communication patterns found in the repo structure. If integrating, follow RESTful conventions for backend APIs.

## Examples
- To add a new backend API: create a controller in `Backend/src/main/.../controllers/`, a service in `.../services/`, and update DTOs as needed.
- To add a new frontend page: add a component in `Frontend/src/pages/` and update routes in `Frontend/src/routes/`.

## References
- Backend: `Backend/README.md`, `Backend/pom.xml`
- Frontend: `Frontend/README.md`, `Frontend/package.json`, `Frontend/vite.config.ts`
- Legacy: `Backend/legacy_backup/`

---
If any conventions or workflows are unclear, please ask for clarification or check the referenced files.