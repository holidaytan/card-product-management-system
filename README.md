# Card Product Management System

CardOps is a Java Spring Boot and React/Material UI application for maintaining card products and their inventory.

## Prerequisites

- Java 25
- Node.js 20 or later
- Docker Desktop (for Microsoft SQL Server)

## Run locally

1. Copy `docker/.env.example` to `docker/.env` and replace `MSSQL_SA_PASSWORD` with a strong SQL Server password.
2. Start SQL Server and create `TESTDB`:

   ```powershell
   docker compose --env-file .env -f docker/docker-compose.yaml up -d
   ```

3. Copy `backend/.env.example` to `backend/.env`, then set `DB_PASSWORD` to the same password. `DB_USERNAME` defaults to `sa`.
4. Start the backend:

   ```powershell
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

   The API is available at `http://localhost:8080`. Flyway applies migrations from `backend/src/main/resources/db/migration`; Hibernate uses `ddl-auto=validate`.

5. Start the frontend in another terminal:

   ```powershell
   cd frontend
   npm ci
   npm run dev
   ```

   The UI is available at `http://localhost:5173`.

## API and third-party integration

- Product pages use `GET /api/v1/card-products?page=0`; pages are fixed at 10 records and sorted by ID.
- Currency catalogue and MYR exchange-rate requests go through CardOps (`/api/v1/exchange-rates/currencies` and `/api/v1/exchange-rates?to=SGD`). The backend calls Frankfurter over HTTPS; the browser never calls the provider directly.
- API request and response metadata/bodies are written to `logs/card-product-management.log`. Bodies are capped and common sensitive JSON/query values are redacted. Log files rotate daily and at 10 MB.
- The exported Postman collection is at `postman/Card-Product-Management.postman_collection.json`.

## Verification

```powershell
cd backend
.\mvnw.cmd test

cd ..\frontend
npm run lint
npx tsc -b
npm run build
```
