# SkillBarter AI — Phase 1: Foundation + Identity + Multi-Tenancy

> AI-Powered Skill Exchange & Learning Marketplace

---

## Project Overview

SkillBarter AI is a multi-tenant, AI-ready skill exchange platform that allows students and professionals within institutions to trade skills peer-to-peer.

**Phase 1** establishes the foundation:
- Secure multi-tenant identity with JWT stateless authentication
- Refresh token rotation and reuse detection
- Role-based access control (SUPER_ADMIN, TENANT_ADMIN, STUDENT)
- Tenant-scoped data isolation enforced at the service layer
- Audit logging for all security events
- In-memory rate limiting for brute-force protection
- Clean SaaS frontend with institution-scoped profiles

---

## Architecture: Modular Monolith

```
skillbarter-ai/
├── backend/          # Spring Boot 3.4 (Java 21)
│   └── src/main/java/com/skillbarter/
│       ├── common/   # Security, exceptions, config, responses
│       ├── tenant/   # Multi-tenancy: entity, repo, service, controller
│       ├── identity/ # Auth: JWT, refresh tokens, login, register
│       └── user/     # User profiles, audit logs, roles
├── frontend/         # React 18 + TypeScript + Vite + Tailwind CSS
└── docker-compose.yml
```

---

## Technology Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.4, Java 21 |
| Security | Spring Security, JJWT (SHA-512), BCrypt (cost 12) |
| Database | MySQL 8.0+ + Flyway migrations |
| ORM | Spring Data JPA / Hibernate |
| Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| Frontend | React 18, TypeScript, Vite |
| Styling | Tailwind CSS 3 |
| State | TanStack Query + React Context |
| HTTP | Axios with interceptors + refresh token rotation |
| Tests | JUnit 5, Mockito, H2 (backend) · Vitest + RTL (frontend) |
| Container | Docker + Docker Compose |

---

## Quick Start

### Prerequisites
- Java 21+
- Node 18+
- MySQL 8.0+ (or Docker)
- Maven Wrapper (`mvnw`) — included

### 1. Configure Environment

```bash
cp .env.example .env
# Edit .env and fill in required values:
#   JWT_SECRET=<at-least-64-character-random-string>
#   DATABASE_PASSWORD=<your-mysql-password>
```

### 2. Start Backend

```bash
cd backend

# With Docker for MySQL only:
docker run -d --name skillbarter-db \
  -e MYSQL_DATABASE=skillbarter \
  -e MYSQL_USER=skillbarter \
  -e MYSQL_PASSWORD=changeme \
  -e MYSQL_ROOT_PASSWORD=root_changeme \
  -p 3306:3306 mysql:8.0

# Run application
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Backend starts at: `http://localhost:8080`
Swagger UI: `http://localhost:8080/swagger-ui.html`
Health: `http://localhost:8080/api/v1/health`

### 3. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend starts at: `http://localhost:5173`

### 4. Docker Compose (Full Stack)

```bash
# Requires Docker Desktop
cp .env.example .env   # Fill in JWT_SECRET and DB_PASSWORD
docker compose up -d
```

---

## Database Migrations (Flyway)

| Migration | Description |
|---|---|
| `V1__initial_schema.sql` | Creates: tenants, users, roles, user_roles, refresh_tokens, audit_logs |

All future schema changes must be new numbered migration files. **Never edit existing migrations.**

---

## API Endpoints (Phase 1)

### Public

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/health` | Health check + DB connectivity |
| `POST` | `/api/v1/auth/register` | Register new user under a tenant slug |
| `POST` | `/api/v1/auth/login` | Authenticate, receive access + refresh tokens |
| `POST` | `/api/v1/auth/refresh` | Rotate refresh token, receive new tokens |
| `POST` | `/api/v1/auth/logout` | Revoke refresh token |

### Protected (Bearer JWT required)

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/users/me` | Get current user profile |
| `PUT` | `/api/v1/users/me` | Update current user profile |
| `GET` | `/api/v1/tenants/me` | Get current tenant |
| `GET` | `/api/v1/tenants/{id}` | Get tenant by ID (TENANT_ADMIN+) |
| `POST` | `/api/v1/tenants` | Create tenant (SUPER_ADMIN only) |

---

## Security Architecture

### Multi-Tenancy Isolation

- `JwtAuthenticationFilter` extracts `tenantId` from JWT and sets `TenantContext`
- ALL service-layer queries include `AND tenant_id = :tenantId` via `UserRepository.findByIdAndTenantId()`
- **Frontend-supplied `tenant_id` is NEVER trusted** — only the JWT-derived context is used
- Test: `TenantIsolationTest` proves Tenant B cannot access Tenant A resources

### JWT Token Flow

```
Login → access_token (15min) + refresh_token (7 days)
  ↓
Access token expires → POST /auth/refresh
  ↓
Old refresh token revoked + new pair issued (rotation)
  ↓
Token reuse detected → ALL user tokens revoked (security event)
```

### Rate Limiting

In-memory sliding window per IP:
- Login: 10 requests per 15 minutes
- Register: 5 requests per 60 minutes

> Phase 2: Replace with Redis-backed Bucket4j for distributed rate limiting

---

## Running Tests

### Backend

```bash
cd backend
./mvnw test
```

| Test | Type | Covers |
|---|---|---|
| `JwtServiceTest` | Unit | Token generation, claim extraction, tamper detection |
| `TenantContextTest` | Unit | ThreadLocal lifecycle |
| `TenantIsolationTest` | Unit (Security) | Cross-tenant data access prevention |

### Frontend

```bash
cd frontend
npm run test
```

| Test | Covers |
|---|---|
| `auth.test.tsx` | Token storage and session clearing |
| `Button.test.tsx` | Component rendering, click handlers, loading state |

---

## Design System

| Token | Value |
|---|---|
| Primary Navy | `#0B1220` |
| Primary Teal | `#14B8A6` |
| Mint Accent | `#5EEAD4` |
| AI Blue | `#3B82F6` |
| Background | `#F8FAFC` |
| Card | `#FFFFFF` |
| Border | `#E2E8F0` |
| Font | Inter |
| Button radius | 8px |
| Card radius | 12px |
| Panel radius | 16px |

---

## What's NOT in Phase 1

The following features are deliberately deferred to future phases:

- [ ] Skill graph and semantic search
- [ ] AI recommendation engine
- [ ] Skill credit ledger
- [ ] Peer-to-peer session scheduling
- [ ] Reputation and review system
- [ ] Email verification
- [ ] Redis-backed rate limiting
- [ ] AWS deployment (ECS/RDS)
- [ ] MLOps pipeline

---

## License

MIT License — See `LICENSE` for details.
