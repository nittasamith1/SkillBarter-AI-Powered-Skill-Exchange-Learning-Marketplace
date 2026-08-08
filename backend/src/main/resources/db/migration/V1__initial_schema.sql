-- ============================================================
-- V1__initial_schema.sql
-- SkillBarter AI — Phase 1 Initial Schema (MySQL 8.0+)
-- ============================================================

-- ── tenants ──────────────────────────────────────────────────
CREATE TABLE tenants (
    id          CHAR(36)        NOT NULL,
    name        VARCHAR(255)    NOT NULL,
    slug        VARCHAR(100)    NOT NULL,
    status      ENUM('ACTIVE', 'SUSPENDED', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_tenants       PRIMARY KEY (id),
    CONSTRAINT uq_tenants_slug  UNIQUE (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── users ─────────────────────────────────────────────────────
CREATE TABLE users (
    id                  CHAR(36)        NOT NULL,
    tenant_id           CHAR(36)        NOT NULL,
    email               VARCHAR(255)    NOT NULL,
    password_hash       VARCHAR(255)    NOT NULL,
    first_name          VARCHAR(100)    NOT NULL,
    last_name           VARCHAR(100)    NOT NULL,
    bio                 TEXT,
    location            VARCHAR(255),
    preferred_language  VARCHAR(50)     DEFAULT 'en',
    status              ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION') NOT NULL DEFAULT 'ACTIVE',
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    -- Email is globally unique across all tenants (simplifies auth lookup)
    CONSTRAINT pk_users         PRIMARY KEY (id),
    CONSTRAINT uq_users_email   UNIQUE (email),
    CONSTRAINT fk_users_tenant  FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── roles ─────────────────────────────────────────────────────
CREATE TABLE roles (
    id      INT             NOT NULL AUTO_INCREMENT,
    name    VARCHAR(50)     NOT NULL,

    CONSTRAINT pk_roles     PRIMARY KEY (id),
    CONSTRAINT uq_roles_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── user_roles ────────────────────────────────────────────────
CREATE TABLE user_roles (
    user_id     CHAR(36)    NOT NULL,
    role_id     INT         NOT NULL,

    CONSTRAINT pk_user_roles            PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user       FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role       FOREIGN KEY (role_id)  REFERENCES roles(id)  ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── refresh_tokens ────────────────────────────────────────────
CREATE TABLE refresh_tokens (
    id          CHAR(36)        NOT NULL,
    user_id     CHAR(36)        NOT NULL,
    token_hash  VARCHAR(255)    NOT NULL,
    expires_at  DATETIME(6)     NOT NULL,
    revoked     TINYINT(1)      NOT NULL DEFAULT 0,
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_refresh_tokens            PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_hash       UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── audit_logs ────────────────────────────────────────────────
CREATE TABLE audit_logs (
    id              CHAR(36)        NOT NULL,
    tenant_id       CHAR(36),
    user_id         CHAR(36),
    action          VARCHAR(100)    NOT NULL,
    resource_type   VARCHAR(100),
    resource_id     VARCHAR(255),
    metadata        JSON,
    ip_address      VARCHAR(45),
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_audit_logs PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Indexes ───────────────────────────────────────────────────
CREATE INDEX idx_users_email              ON users(email);
CREATE INDEX idx_users_tenant_id          ON users(tenant_id);
CREATE INDEX idx_users_tenant_email       ON users(tenant_id, email(191));
CREATE INDEX idx_users_status             ON users(status);
CREATE INDEX idx_refresh_tokens_hash      ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_user_id   ON refresh_tokens(user_id);
CREATE INDEX idx_audit_logs_tenant_date   ON audit_logs(tenant_id, created_at);
CREATE INDEX idx_audit_logs_user_id       ON audit_logs(user_id);

-- ── Seed data — Roles ─────────────────────────────────────────
INSERT INTO roles (name) VALUES
    ('SUPER_ADMIN'),
    ('TENANT_ADMIN'),
    ('STUDENT');

-- ── Seed data — Default Tenant ───────────────────────────────
INSERT INTO tenants (id, name, slug, status)
VALUES ('00000000-0000-0000-0000-000000000001', 'SkillBarter Platform', 'skillbarter', 'ACTIVE');
