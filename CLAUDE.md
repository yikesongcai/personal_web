# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

### Backend (Spring Boot 3.3.4 / Java 17 / Maven)

```bash
cd backend
mvn spring-boot:run                           # dev server on :8080
mvn clean package -DskipTests                 # production JAR
mvn test                                      # run single test file
```

Tests use JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`).

### Frontend (Vue 3 / Vite)

```bash
cd frontend
npm run dev                                   # Vite dev server on :5173
npm run build                                 # production build to dist/
```

### Docker Compose (full stack)

```bash
docker compose up -d --build
```

Requires an external Docker network `core-network`. Backend on internal :8080, frontend Nginx exposed on :8082.

## Architecture

### Backend — `com.personalweb.ai`

Spring Boot 3 WebFlux (reactive) backend using **JdbcTemplate** (not JPA) for data access and **Spring AI** for LLM integration (OpenAI-compatible chat + embeddings, Alibaba Cloud DashVector as vector store).

**Request flow:** `WebFilter` chain (security headers → rate limit → admin auth) → `@RestController` → Service → DAO (`JdbcTemplate`).

**WebFlux filters** (applied in this order):
- `SecurityHeadersFilter` — security headers on all responses
- `AdminAuthFilter` — checks `X-Admin-Token` header against `app.admin.api-key` for paths under `/api/admin`, `/api/knowledge`, `/api/debug`
- `RateLimitFilter` — sliding-window rate limiter (10 req/60s per IP) on `/api/chat`, with database-backed IP whitelist

**Public API:** `/api/home`, `/api/projects`, `/api/articles`, `/api/chat` (SSE streaming with RAG), `/api/chat/history/{sessionId}`

**Admin API** (all require `X-Admin-Token`): CRUD for projects/articles (soft-delete via `deleted=1`), site config key-value store, file upload, rate-limit whitelist, dashboard stats, system logs

**Key patterns:**
- All DELETE operations are soft deletes: `UPDATE ... SET deleted = 1`
- All SELECT queries filter `WHERE deleted = 0`
- Articles and projects have `sort_order` (ASC) and `is_featured` columns; list queries use `ORDER BY sort_order ASC, id DESC`
- Content changes (articles/projects) auto-sync to DashVector for RAG via `KnowledgeIngestionService`
- `DataInitializer` (`CommandLineRunner`) handles schema migrations and seed data idempotently on startup — add new tables/columns there rather than with SQL migration files
- `RateLimitWhitelistService` caches whitelisted IPs in memory, refreshed every 60s via `@Scheduled`
- `SiteConfigService` provides key-value config backed by `site_config` table

**Exception handling:** `BusinessException` (custom code + HttpStatus) caught by `GlobalExceptionHandler` (`@RestControllerAdvice`). Factory methods: `BusinessException.badRequest()`, `.externalService()`, `.internal()`.

**Config:** All secrets via environment variables. `spring-dotenv` loads `.env` file for local dev. `management.endpoints.web.exposure.include: health,info,metrics` for actuator. `spring.codec.max-in-memory-size: 10MB`.

### Frontend — Vue 3 SPA

Vue 3 Composition API with `vue-router` (HTML5 history mode). No state management library — each component manages its own state with inline `fetch` calls. All API calls use relative paths (proxied by Vite in dev, same-origin in production via Nginx).

**Routes:**
| Path | View | Auth |
|------|------|------|
| `/` | `Home.vue` | public |
| `/projects` | `Projects.vue` | public |
| `/projects/:id` | `ProjectDetail.vue` | public |
| `/articles` | `Articles.vue` | public |
| `/articles/:id` | `ArticleDetail.vue` | public |
| `/admin/login` | `AdminLogin.vue` | public |
| `/admin/*` | `Admin.vue` (layout) + child routes | requires `adminToken` in `sessionStorage` |

**Admin child routes:** `dashboard`, `homepage`, `projects`, `articles`, `rate-limit`, `system-log`

**Key patterns:**
- Auth is client-side only: API key stored in `sessionStorage`, sent as `X-Admin-Token` header
- `ProjectDetail.vue` and `ArticleDetail.vue` contain identical code (they detect type from `route.path.startsWith('/projects')`) — changes to one should be reflected in the other
- `AiChatWidget.vue` is rendered globally in `App.vue`, manages its own UUID-based session in `localStorage`
- Markdown rendering uses `marked` + `DOMPurify` for XSS sanitization
- Admin dashboard uses ECharts for charts

### Infrastructure

Nginx reverse proxy in front of the frontend container: serves static files from `/usr/share/nginx/html`, proxies `/api/*` to `personal_web_backend:8080` with SSE-friendly config (`proxy_buffering off`, `proxy_read_timeout 3600s`).

**Volumes:** `./logs:/app/logs`, `./backend/uploads:/app/uploads` — uploaded files from `/api/admin/upload` are stored in `uploads/` and served by WebFlux resource handler at `/api/files/**`.

### Database (MySQL)

Core tables: `article`, `project`, `chat_turn`, `daily_stats`, `system_log`, `site_config`, `rate_limit_whitelist`. All tables created/evolved by `DataInitializer` on startup.
