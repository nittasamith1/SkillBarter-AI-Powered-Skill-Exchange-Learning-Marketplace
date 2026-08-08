# SkillBarter AI — AI-Powered Skill Exchange & Learning Marketplace

> Connect with institutional peers for peer-to-peer skill exchanges powered by multi-tenant security, skill matching, and AI learning roadmaps.

---

## 🌟 Project Overview

**SkillBarter AI** is a multi-tenant, AI-ready skill exchange platform where students and professionals within institutions trade skills peer-to-peer (e.g. *"I will teach you Java OOP in exchange for learning System Design or React"*).

The platform is designed as a **Modular Monolith** with clean domain boundary separation, stateless JWT security, strict institutional multi-tenancy, and a modern **Clean Light SaaS UI** (Linear × Notion × fintech aesthetic).

---

## 🚀 Key Features

### 🟢 Phase 1 — Foundation & Security Infrastructure
- **Multi-Tenant Isolation:** Tenant identity derived strictly from JWT authentication context (`TenantContext` ThreadLocal). Cross-tenant data leakage is impossible by design.
- **Stateless JWT & Token Rotation:** Short-lived access tokens (15 min) + long-lived refresh tokens (7 days) with rotation and automatic reuse detection/revocation.
- **RBAC Security:** Roles (`SUPER_ADMIN`, `TENANT_ADMIN`, `STUDENT`) with Spring Security authorization rules.
- **Audit Logging:** System security logs for login events, token reuse detection, and sensitive actions.
- **Rate Limiting:** Sliding-window rate limiting on login/registration endpoints.

### 🟢 Phase 2 — Skill Ecosystem & Core Marketplace
- **Skill Taxonomy & Catalog:** Hierarchical skill categories (`Programming`, `Design`, `DevOps`, etc.), skills, and skill prerequisites.
- **User Skill Profiles:** Users specify skills they **Can Teach** (with proficiency level and years of experience) and skills they **Want to Learn**.
- **Learning Goals:** Define target skills, current level, desired target level (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `EXPERT`), completion deadlines, and learning preferences.
- **Marketplace & Peer Discovery:**
  - Search skills by name or category filter.
  - **Inline Peer Discovery:** Discover all qualified peers in your institution who teach a selected skill.
  - **Skill Exchange Requests:** Propose exchange requests specifying *"Skill I Offer"* vs *"Skill I Want"* with custom messages.
  - **Request Lifecycle Management:** Pending exchange request alerts on the dashboard with instant **Accept** / **Decline** actions.
- **Skill Match Recommendation Engine:** Matches peers whose offered teachable skills align with your declared learning targets and goals.

---

## 🎨 Design System

Built with a **Clean Light SaaS** direction (*"Human + Technology + Trust"*):

| Token | Hex Code | Purpose |
|---|---|---|
| **Primary Navy** | `#0B1220` | Sidebar background, hero left panels, primary text headings |
| **Primary Teal** | `#14B8A6` | Primary action buttons, active navigation indicator, key icons |
| **Growth Mint** | `#5EEAD4` | Mint badges, hover highlights, progress elements |
| **AI Accent** | `#3B82F6` | AI match recommendation card accents (`#14B8A6` → `#3B82F6`) |
| **App Canvas** | `#F8FAFC` | Main content canvas area |
| **Card Surface** | `#FFFFFF` | All content cards, modals, and input fields |
| **Border Color** | `#E2E8F0` | Subtle clean card borders (no heavy drop shadows) |
| **Primary Text** | `#0F172A` | Card titles, user names, main text hierarchy |
| **Secondary Text** | `#64748B` | Subtitles, helper text, category labels |

---

## 🛠️ Technology Stack

### Backend
- **Framework:** Spring Boot 3.4 (Java 21)
- **Security:** Spring Security, JJWT (SHA-512), BCrypt (cost 12)
- **Database:** MySQL 8.0+ + Flyway schema migrations
- **ORM:** Spring Data JPA / Hibernate (`CHAR(36)` UUID mapping via `@JdbcTypeCode`)
- **API Docs:** SpringDoc OpenAPI 3 (Swagger UI)

### Frontend
- **Framework:** React 18, TypeScript, Vite
- **Styling:** Tailwind CSS 3 (Vanilla CSS utility extensions)
- **Icons:** Lucide React
- **HTTP Client:** Axios with request/response interceptors + automatic JWT refresh rotation

---

## 📂 Project Architecture

```text
skillbarter-ai/
├── backend/
│   ├── src/main/java/com/skillbarter/
│   │   ├── common/         # Security (JWT, TenantContext), exceptions, config, CORS
│   │   ├── tenant/         # Institution tenant entity & services
│   │   ├── identity/       # Auth: Login, Register, Refresh Token Rotation
│   │   ├── user/           # User profile management & security audit logging
│   │   ├── skill/          # Skill taxonomy, categories, user skills, learning goals
│   │   └── marketplace/    # Exchange requests & peer match recommendation engine
│   └── src/main/resources/
│       └── db/migration/   # Flyway V1 (Identity) & V2 (Skill Ecosystem) migrations
├── frontend/
│   ├── src/
│   │   ├── components/     # UI primitives (Button, Card, Input, Badge), Layout (Navbar, Sidebar)
│   │   ├── features/       # Feature pages: auth, dashboard, skills, goals, marketplace, profile
│   │   ├── lib/            # Auth context provider & API client helper functions
│   │   └── types/          # TypeScript interface definitions
│   └── index.html
└── docker-compose.yml
```

---

## 🚦 Quick Start Guide

### Prerequisites
- **Java 21** or higher
- **Node.js 18** or higher
- **MySQL 8.0** or Docker

### 1. Environment Setup

Copy `.env.example` to `.env` in the root directory:

```bash
cp .env.example .env
```

Ensure your `.env` contains:
```env
JWT_SECRET=your_super_secret_jwt_key_that_is_at_least_64_characters_long_for_security
DATABASE_URL=jdbc:mysql://localhost:3306/skillbarter?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DATABASE_USERNAME=skillbarter
DATABASE_PASSWORD=changeme
```

### 2. Run Database (Docker option)

```bash
docker run -d --name skillbarter-db \
  -e MYSQL_DATABASE=skillbarter \
  -e MYSQL_USER=skillbarter \
  -e MYSQL_PASSWORD=changeme \
  -e MYSQL_ROOT_PASSWORD=root_changeme \
  -p 3306:3306 mysql:8.0
```

### 3. Start Backend (Spring Boot)

```bash
cd backend
./mvnw spring-boot:run
```
*or on Windows PowerShell:*
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

- **API Base:** `http://localhost:8080/api/v1`
- **Swagger UI Docs:** `http://localhost:8080/swagger-ui.html`
- **Health Check:** `http://localhost:8080/api/v1/health`

### 4. Start Frontend (React + Vite)

```bash
cd frontend
npm install
npm run dev
```

- **Frontend Application:** `http://localhost:5173`

---

## 🗄️ Database Schema Migrations (Flyway)

| Migration File | Description |
|---|---|
| `V1__initial_schema.sql` | Core identity: `tenants`, `users`, `roles`, `user_roles`, `refresh_tokens`, `audit_logs` |
| `V2__skill_ecosystem.sql` | Marketplace ecosystem: `skill_categories`, `skills`, `skill_prerequisites`, `user_skills`, `learning_goals`, `exchange_requests` |

---

## 🔗 Key REST API Endpoints

### Auth & Multi-Tenancy
- `POST /api/v1/auth/register` — Register user under an institution tenant
- `POST /api/v1/auth/login` — Authenticate user & receive JWT access + refresh tokens
- `POST /api/v1/auth/refresh` — Rotate refresh token for new access token
- `GET /api/v1/users/me` — Get current user profile

### Skill Management & Catalog
- `GET /api/v1/skill-categories` — List all skill categories
- `GET /api/v1/skills` — Search & explore skill catalog
- `GET /api/v1/users/me/skills` — Get current user's teachable & learnable skills
- `POST /api/v1/users/me/skills` — Add skill to user profile
- `DELETE /api/v1/users/me/skills/{skillId}` — Remove skill from profile

### Learning Goals
- `GET /api/v1/users/me/learning-goals` — Get user's learning goals
- `POST /api/v1/users/me/learning-goals` — Create a new learning goal
- `DELETE /api/v1/users/me/learning-goals/{id}` — Delete a learning goal

### Marketplace & Peer Exchange
- `GET /api/v1/marketplace/users` — Search peer user profiles by skill
- `GET /api/v1/marketplace/users/{id}` — Get public peer profile
- `POST /api/v1/exchange-requests` — Send a skill exchange request
- `PUT /api/v1/exchange-requests/{id}/status` — Accept or decline an exchange request
- `GET /api/v1/dashboard` — Fetch complete dashboard summary (pending requests, skills, goals, and AI match recommendations)

---

## 🧪 Testing

### Backend Unit & Integration Tests
```bash
cd backend
./mvnw test
```
*Includes tests for `JwtServiceTest`, `TenantContextTest`, and `TenantIsolationTest`.*

### Frontend Vitest Suite
```bash
cd frontend
npm run test
```

### Production Build Check
```bash
cd frontend
npm run build
```

---

## 📄 License

MIT License — see `LICENSE` for details.
