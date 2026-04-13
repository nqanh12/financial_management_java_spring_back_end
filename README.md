# Expense Management Backend

Production-oriented **Spring Boot 4** API for personal and shared expenses: wallets, categories, transactions, monthly budgets with overrun alerts, cached analytics, and group split bills. The codebase follows **Clean Architecture** layering (domain, application, infrastructure, presentation) with **outbound ports** implemented by persistence/cache adapters.

## Architecture

- **Domain**: enums (`UserRole`, `CategoryType`, `TransactionDirection`, `SplitType`), shared rules such as `EqualSplitCalculator`, and domain exceptions.
- **Application**: use-case services, DTOs, and `port.out` interfaces (e.g. `WalletPort`, `ReportingCachePort`).
- **Infrastructure**: JPA entities/repositories, MapStruct mappers, Redis cache-aside for reports, OAuth2 Google login + JWT for APIs, Flyway migrations.
- **Presentation**: REST controllers under `/api/v1`, global exception handler, OpenAPI/Swagger.

```mermaid
flowchart LR
  subgraph presentation [presentation]
    C[Controllers]
  end
  subgraph application [application]
    S[Services]
    P[Ports out]
  end
  subgraph infrastructure [infrastructure]
    A[Adapters]
    R[Repositories]
    Sec[Security]
  end
  C --> S
  S --> P
  A --> P
  A --> R
  Sec --> C
```

## Tech stack

- Java 17, Spring Boot 4, Spring Security (OAuth2 Client + JWT), Spring Data JPA, PostgreSQL, Redis, Flyway, springdoc OpenAPI, Lombok, MapStruct, JUnit 5 + Mockito.

## Prerequisites

- JDK 17+, Maven 3.9+
- PostgreSQL 16+ and Redis 7+ (or Docker Compose)

## Security note (credentials)

The repository must not contain real OAuth client secrets or production JWT keys. If those values were ever committed, **rotate them** in Google Cloud Console and reissue your signing secret.

- Base `application.yaml` uses empty defaults for `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, and `JWT_SECRET`. For local development, copy [`application-local.example.yaml`](src/main/resources/application-local.example.yaml) to `application-local.yaml` (gitignored) and fill in real values.
- With profile **`prod`**, the app **fails fast** at startup if any of those three are missing or blank (`ProdRequiredSecretsEnvironmentPostProcessor`).

## Configuration

| Variable | Purpose |
|----------|---------|
| `JDBC_DATABASE_URL` | (Optional) Full JDBC URL; when set, overrides other DB URL rules below |
| `DATABASE_URL` | Render/Heroku style `postgresql://user:pass@host:port/db` → mapped to JDBC + datasource user/pass at startup |
| `DATABASE_HOST`, `DATABASE_PORT`, `DATABASE_NAME`, `DATABASE_USER`, `DATABASE_PASSWORD` | Compose JDBC when `DATABASE_URL` / `JDBC_DATABASE_URL` are unset (local or manual Render env) |
| `DATABASE_SSL_MODE` | JDBC `sslmode` query param (e.g. `require` on Render Postgres, `disable` locally — default in `application.yaml`) |
| `REDIS_URL` | Redis connection string (Render Key Value); local default `redis://localhost:6379` |
| `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` | Google OAuth2 |
| `JWT_SECRET` | HS256 signing material: Base64/Base64URL or any strong string (hashed to 32 bytes internally) |
| `JWT_EXPIRATION_SECONDS` | JWT lifetime (default 86400) |
| `OAUTH2_FRONTEND_REDIRECT_URL` | After Google login, user is redirected here with `accessToken` and `expiresIn` query params |
| `APP_CORS_ORIGINS` | Allowed browser origins (comma-separated list in `application.yaml` as a list) |
| `APP_RATE_LIMIT_PER_MINUTE` | Redis fixed-window cap per authenticated user or per client IP (default 120 per minute) |
| `APP_RATE_LIMIT_WINDOW_SECONDS` | Window length for that cap (default 60) |
| `management.server.port` | (Optional) Bind Spring Boot Actuator on a separate management port in production; see comments in `application-prod.yaml` |

## Local run (Maven)

1. Start PostgreSQL and Redis (e.g. `docker compose up -d postgres redis`).
2. Provide OAuth/JWT secrets via `application-local.yaml` (recommended) or environment variables (`GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `JWT_SECRET`).
3. `mvn spring-boot:run`

`server.forward-headers-strategy: framework` is enabled so `X-Forwarded-For` is honored when you run behind a reverse proxy; restrict who can send those headers at the network edge.

Swagger UI: `http://localhost:8080/swagger-ui.html`  
OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Deploy on Render

1. Push this repo to GitHub (already contains [`render.yaml`](render.yaml)).
2. In [Render Dashboard](https://dashboard.render.com) → **New** → **Blueprint** → connect the repo → **Apply** (creates Postgres, Key Value Redis, and Docker web service `expense-api`).
3. Open the web service → **Environment** → set `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `OAUTH2_FRONTEND_REDIRECT_URL` (URL frontend sau login), and `APP_CORS_ORIGINS` (origins frontend, comma-separated).
4. In [Google Cloud Console](https://console.cloud.google.com/) → OAuth client → **Authorized redirect URIs**, add  
   `https://<your-render-service-name>.onrender.com/login/oauth2/code/google`.
5. After deploy, check `https://<your-service>.onrender.com/actuator/health` and Swagger at `/swagger-ui.html`.

## Docker Compose (app + Postgres + Redis)

```bash
docker compose up --build
```

Uses profile `docker` ([`application-docker.yaml`](src/main/resources/application-docker.yaml)) so the app points at the `postgres` and `redis` service names.

## Authentication flow

1. Browser navigates to `/oauth2/authorization/google`.
2. After success, [`OAuth2LoginSuccessHandler`](src/main/java/com/financialmanagement/expense/infrastructure/security/OAuth2LoginSuccessHandler.java) upserts the user and redirects to `OAUTH2_FRONTEND_REDIRECT_URL` with `accessToken` (JWT) and `expiresIn`.
3. Call APIs with header `Authorization: Bearer <token>`.
4. `GET /api/v1/users/me` returns the persisted profile.

Roles: `USER` (default) and `ADMIN`. Example admin-only endpoint: `GET /api/v1/admin/ping`.

## API overview (`/api/v1`)

| Area | Endpoints |
|------|-----------|
| Users | `GET /users/me` |
| Wallets | CRUD `/wallets` — response includes `description`, `openingBalance`, and computed `currentBalance` (= `openingBalance` + sum(IN) − sum(OUT) over active transactions). |
| Categories | CRUD `/categories` (income/expense) |
| Transactions | CRUD `/transactions` (validates category type vs IN/OUT). Optional `externalReference` (e.g. bank ref); free-text `note` for payee/context. List: omit `page`/`size` for a full array (legacy); pass `page` (0-based) and optional `size` (max 100, default 20) and optional `sort` (`transactionDate` or `amount`, with optional `,ASC`/`,DESC`) for `{ content, page, size, totalElements, totalPages }`. |
| Budgets | `GET/POST /budgets`, `DELETE /budgets/{id}`, `GET /budgets/alerts` — same optional pagination as transactions (`sort`: `yearMonth` or `limitAmount`; alerts: `createdAt` or `yearMonth`). |
| Reports | `GET /reports/dashboard?month=yyyy-MM`, `GET /reports/monthly?month=yyyy-MM` |
| Groups | `POST/GET /groups`, `GET /groups/{id}`, `POST /groups/{id}/members`, shared expenses under `/groups/{id}/shared-expenses` |

## Design decisions

- **JWT after OAuth**: SPA-friendly redirect carries the token; APIs stay stateless aside from optional OAuth session during the login redirect.
- **Soft delete**: `deleted_at` on core aggregates; queries filter active rows.
- **Money**: `NUMERIC(19,4)` in PostgreSQL; validation on DTOs.
- **Cache-aside**: Dashboard and monthly report responses are cached in Redis (`emo:dashboard:…`, `emo:report:…`); mutations evict all keys for that user.
- **Budget alerts**: When OUT transactions push category spend over the monthly limit, a row is upserted in `budget_alerts` (one active row per user/category/month; `updated_at` maintained). Duplicate historical rows were deduplicated in migration `V6`.
- **Rate limiting**: Authenticated requests are keyed by user id; otherwise by client IP (after forwarded-header resolution). Over limit: HTTP **429** with the same JSON shape as other API errors and a **`Retry-After`** header (seconds).
- **Observability**: Logs include `traceId` / `spanId` when present (Micrometer Tracing + Brave). For JSON log lines, add an encoder such as Logstash Logback in your deployment.
- **Group splits**: `EQUAL` uses stable per-member shares with remainder on the last member; `CUSTOM` validates the sum equals the total.
- **Wallet balance**: `opening_balance` seeds the book (e.g. cash on hand when you start tracking). `currentBalance` is derived; no separate beneficiary entity — use `note` (and optionally `externalReference`) on transactions.

## Database migrations

Flyway scripts live under [`src/main/resources/db/migration`](src/main/resources/db/migration). Production uses `ddl-auto: validate`.

## Tests

```bash
mvn test
```

- Mockito-based service tests (e.g. [`TransactionServiceTest`](src/test/java/com/financialmanagement/expense/application/service/TransactionServiceTest.java)).
- [`ExpenseApplicationTests`](src/test/java/com/financialmanagement/expense/ExpenseApplicationTests.java) loads the Spring context with profile `test` (H2, Redis auto-config disabled, in-memory reporting cache).

## CI

[`.github/workflows/ci.yml`](.github/workflows/ci.yml) runs `mvn verify` and builds the Docker image (no push unless you extend the workflow with registry credentials).
