# Card Product Management System

**CardOps** is a full-stack card product management application built with **Java Spring Boot**, **React**, **Material UI**, and **Microsoft SQL Server**.

The application supports:

- Product creation and updates
- Product listing with fixed server-side pagination
- Quantity increment and decrement with a minimum of `0`
- Product deletion only when quantity is `0`
- Product search and card-type filtering
- Global product and inventory summaries
- Display-currency conversion through a backend-integrated third-party API
- Request and response API logging
- Optimistic locking and idempotent product creation

---

## Tech Stack

### Backend

- Java 25
- Spring Boot
- Spring MVC
- Spring Data JPA
- Hibernate
- Flyway
- Microsoft SQL Server
- Caffeine Cache
- Spring `RestClient`

### Frontend

- React
- TypeScript
- Vite
- Material UI
- React Hooks

### Infrastructure

- Docker Compose
- Microsoft SQL Server 2022
- Postman

---

## Prerequisites

Install the following before running the project:

- JDK 25
- Node.js 20 or later
- Docker Desktop
- DBeaver (optional, for database inspection)

Ensure both `java` on `PATH` and `JAVA_HOME` point to Java 25.

Verify:

```powershell
java -version
```

---

# Running the Application Locally

Run the following commands from the repository root unless stated otherwise.

## 1. Start SQL Server

Copy:

```text
docker/.env.example
```

to:

```text
docker/.env
```

Set a valid SQL Server password:

```env
MSSQL_SA_PASSWORD=YourStrongPassword
```

Start the database:

```powershell
docker compose --env-file docker/.env -f docker/docker-compose.yaml up -d
```

Check container status:

```powershell
docker compose --env-file docker/.env -f docker/docker-compose.yaml ps -a
```

Wait until:

- `sqlserver` is healthy
- `db-init` exits with code `0`

The `db-init` container automatically creates:

```text
TESTDB
```

Manual database creation is not required.

---

## 2. Verify TESTDB with DBeaver (Optional)

Before starting the backend, you can verify that SQL Server and `TESTDB` are available using DBeaver.

Create a new **Microsoft SQL Server** connection with:

| Setting | Value |
|---|---|
| Host | `localhost` |
| Port | `1433` |
| Database | `TESTDB` |
| Authentication | SQL Server authentication |
| Username | `sa` |
| Password | Value configured in `docker/.env` |
| Trust server certificate | `true` for local development |

Click **Test Connection**.

A successful connection confirms that:

- SQL Server is running
- Port `1433` is reachable
- `TESTDB` was created successfully
- The configured credentials are valid

At this stage, the application tables may not exist yet.

They are created and migrated by Flyway when the Spring Boot backend starts.

After the backend starts, refresh:

```text
TESTDB
└── dbo
```

to view:

- Card product tables
- Idempotency records
- Flyway schema history

Do not create application tables manually in DBeaver.

---

## 3. Configure the Backend

Copy:

```text
backend/.env.example
```

to:

```text
backend/.env
```

Use the same SQL Server password configured in `docker/.env`.

Example:

```env
DB_USERNAME=sa
DB_PASSWORD=YourStrongPassword
```

---

## 4. Start the Backend

Open a PowerShell terminal:

```powershell
cd backend

java -version
.\mvnw.cmd --version
```

Both commands should report Java 25.

Start Spring Boot:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend URL:

```text
http://localhost:8080
```

On startup:

- Spring connects to SQL Server `TESTDB`
- Flyway applies database migrations
- Hibernate validates the schema using `ddl-auto=validate`

After startup, you can refresh DBeaver to inspect the generated schema and seeded data.

---

## 5. Check Backend Health

In another terminal:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Expected result:

```text
groups                status
------                ------
{liveness, readiness} UP  
```

The health check includes database connectivity.

If the endpoint is unavailable, check:

- Spring Boot terminal output
- SQL Server container status
- Local database credentials

The health endpoint does not verify Frankfurter API availability.

---

## 6. Start the Frontend

From the repository root:

```powershell
cd frontend
npm ci
npm run dev
```

Frontend URL:

```text
http://localhost:5173
```

---

# Configuration

| File | Purpose |
|---|---|
| `backend/src/main/resources/application.yaml` | Shared JPA, Flyway, logging, health, cache, and exchange-rate configuration |
| `backend/src/main/resources/application-dev.yaml` | Local SQL Server configuration using `TESTDB` |
| `backend/.env.example` | Backend database credential template |
| `docker/docker-compose.yaml` | SQL Server and database initialization services |
| `docker/init/create-testdb.sql` | Automatically creates `TESTDB` |
| `docker/.env.example` | SQL Server password template |
| `frontend/.env.example` | Optional frontend API base URL configuration |

Real `.env` files are excluded from Git.

---

# Database

The application uses Microsoft SQL Server with:

```text
Database: TESTDB
Host: localhost
Port: 1433
```

Database schema changes are managed through Flyway migrations located at:

```text
backend/src/main/resources/db/migration
```

Hibernate validates the migrated schema rather than generating tables automatically.

---

# API Overview

## Product Listing

```http
GET /api/v1/card-products?page=0
```

Pagination is fixed at:

```text
10 records per page
```

Pages are zero-based:

```text
page=0 → first 10 records
page=1 → next 10 records
```

Product search and card-type filters apply across the full product dataset. The matching results are then returned using the existing fixed pagination of 10 records per page.

Example:

```http
GET /api/v1/card-products?page=0&name=platinum&type=CREDIT
```

This returns the first page of products matching `platinum` with card type `CREDIT`.


---

## Product Summary

```http
GET /api/v1/card-products/summary
```

Returns global product and inventory totals.

Example:

```json
{
  "totalProducts": 33,
  "totalAvailableInventory": 5110
}
```

---

## Product Maintenance

The backend supports:

```http
POST   /api/v1/card-products
GET    /api/v1/card-products/{id}
PUT    /api/v1/card-products/{id}
DELETE /api/v1/card-products/{id}
```

Product deletion is only allowed when quantity is `0`.

Create requests support an `Idempotency-Key` header to prevent duplicate submissions.

Updates use optimistic locking through the product version field.

---

## Third-Party API Integration

CardOps integrates with the **Frankfurter exchange-rate API** through the Spring Boot backend.

Available CardOps endpoints include:

```http
GET /api/v1/exchange-rates/currencies
```

Returns the supported currency catalogue.

```http
GET /api/v1/exchange-rates?to=SGD
```

Returns the exchange rate from the application's base currency, **MYR**, to the requested currency.

The request flow is:

```text
React / Postman
      ↓
CardOps Spring Boot API
      ↓
Frankfurter API
```

The frontend never calls Frankfurter directly.

**MYR is the application's base and stored currency.** Selecting another currency in the frontend only converts annual fees for display purposes; the original product pricing stored in the database remains in MYR.

---

## Exchange-Rate Resilience

Frankfurter availability does not affect product maintenance. Exchange-rate data is held in the in-memory Caffeine cache:

- The currency catalogue is cached for 24 hours.
- Each MYR-to-target-currency rate is cached for 1 hour.

Cached data remains available until its configured expiry if Frankfurter is temporarily unavailable. After a cache miss or expiry, an unavailable provider returns HTTP `503`; a provider timeout returns HTTP `504`.

If currency conversion cannot be retrieved, the frontend resets the display currency to MYR and shows a warning. Product prices remain available because annual fees are stored in MYR.

The cache durations can be changed with `EXCHANGE_RATE_CURRENCY_CACHE_TTL` and `EXCHANGE_RATE_CACHE_TTL`. There is currently no secondary exchange-rate provider or persistent last-known-rate fallback.

---

# API Request and Response Logging

Requests under:

```text
/api/**
```

are logged to:

```text
backend/logs/card-product-management.log
```

Logged information includes:

- HTTP method
- Request path
- Query parameters
- Request body
- Response status
- Response body
- Request duration

Payload size is capped and common sensitive fields are redacted.

Log files use rotation based on date and file size.

---

# Postman Collection

The exported Postman collection is available at:

```text
postman/Card-Product-Management.postman_collection.json
```

It includes requests for:

- Paginated product listing
- Search and filtering
- Product summary
- Product creation
- Product updates
- Product deletion
- Zero-quantity deletion validation
- Currency catalogue
- Exchange-rate conversion

Default backend URL:

```text
http://localhost:8080
```

---

# Verification

## Backend

```powershell
cd backend
.\mvnw.cmd test
```

## Frontend

```powershell
cd frontend

npm test
npm run test:coverage
npm run lint
npx tsc -b
npm run build
```

The frontend test suite uses Vitest, React Testing Library, and Mock Service Worker. It includes API-client unit tests, product-form component tests, and dashboard integration coverage for an unavailable currency provider.

---

# Troubleshooting

## Java Class Version Mismatch

If Spring Boot fails with:

```text
UnsupportedClassVersionError
```

and mentions:

```text
class file version 69.0
```

Java 25 was used to compile the application.

If your runtime only recognizes:

```text
65.0
```

then Maven is likely running with Java 21.

Verify:

```powershell
java -version
.\mvnw.cmd --version
```

Both should report Java 25.

You can temporarily update the current terminal:

```powershell
$env:JAVA_HOME = 'C:\Program Files\OpenJDK\jdk-25'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

java -version
.\mvnw.cmd --version
.\mvnw.cmd clean spring-boot:run
```

Adjust the JDK path to match your local installation.

---

## Docker Environment File Not Found

When running commands from the repository root, use:

```powershell
docker compose --env-file docker/.env -f docker/docker-compose.yaml up -d
```

If running inside the `docker` directory:

```powershell
docker compose --env-file .env -f docker-compose.yaml up -d
```

Ensure `docker/.env` has been created from:

```text
docker/.env.example
```

---

# Project Structure

```text
card-product-management-system/
├── backend/
│   ├── src/
│   ├── .env.example
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   ├── .env.example
│   └── package.json
│
├── docker/
│   ├── docker-compose.yaml
│   ├── .env.example
│   └── init/
│       └── create-testdb.sql
│
├── postman/
│   └── Card-Product-Management.postman_collection.json
│
└── README.md
```

---

# Notes

- SQL Server database name: `TESTDB`
- Product pagination is fixed at 10 records per page
- Product quantity cannot be negative
- Products can only be deleted when quantity is `0`
- Annual fees are stored in MYR
- Currency conversion is display-only
- Third-party exchange-rate calls are made through the backend
- Real environment files and logs should not be committed
