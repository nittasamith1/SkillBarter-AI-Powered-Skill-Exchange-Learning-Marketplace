-- ============================================================
-- V3__exchange_engine.sql
-- SkillBarter AI — Phase 3: Exchange Engine
-- MySQL 8.0+
-- ============================================================

-- ── user_availability ─────────────────────────────────────────
CREATE TABLE user_availability (
    id              CHAR(36)        NOT NULL,
    user_id         CHAR(36)        NOT NULL,
    tenant_id       CHAR(36)        NOT NULL,
    day_of_week     ENUM('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') NOT NULL,
    start_time      TIME            NOT NULL,
    end_time        TIME            NOT NULL,
    timezone        VARCHAR(100)    NOT NULL DEFAULT 'UTC',
    active          TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_user_availability         PRIMARY KEY (id),
    CONSTRAINT fk_availability_user         FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE CASCADE,
    CONSTRAINT fk_availability_tenant       FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT,
    CONSTRAINT chk_availability_times       CHECK (start_time < end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── sessions ──────────────────────────────────────────────────
CREATE TABLE sessions (
    id                  CHAR(36)        NOT NULL,
    tenant_id           CHAR(36)        NOT NULL,
    exchange_request_id CHAR(36)        NOT NULL,
    teacher_id          CHAR(36)        NOT NULL,
    learner_id          CHAR(36)        NOT NULL,
    skill_id            CHAR(36)        NOT NULL,
    scheduled_start     DATETIME(6)     NOT NULL,
    scheduled_end       DATETIME(6)     NOT NULL,
    timezone            VARCHAR(100)    NOT NULL DEFAULT 'UTC',
    status              ENUM('SCHEDULED','IN_PROGRESS','COMPLETED','CANCELLED','NO_SHOW','DISPUTED') NOT NULL DEFAULT 'SCHEDULED',
    meeting_link        VARCHAR(1000),
    cancellation_reason TEXT,
    credits_settled     TINYINT(1)      NOT NULL DEFAULT 0,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_sessions                  PRIMARY KEY (id),
    CONSTRAINT fk_sessions_tenant           FOREIGN KEY (tenant_id)           REFERENCES tenants(id)          ON DELETE RESTRICT,
    CONSTRAINT fk_sessions_exchange_req     FOREIGN KEY (exchange_request_id) REFERENCES exchange_requests(id) ON DELETE RESTRICT,
    CONSTRAINT fk_sessions_teacher          FOREIGN KEY (teacher_id)          REFERENCES users(id)             ON DELETE RESTRICT,
    CONSTRAINT fk_sessions_learner          FOREIGN KEY (learner_id)          REFERENCES users(id)             ON DELETE RESTRICT,
    CONSTRAINT fk_sessions_skill            FOREIGN KEY (skill_id)            REFERENCES skills(id)            ON DELETE RESTRICT,
    CONSTRAINT chk_sessions_times          CHECK (scheduled_start < scheduled_end),
    CONSTRAINT chk_sessions_users          CHECK (teacher_id <> learner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── credit_wallets ────────────────────────────────────────────
CREATE TABLE credit_wallets (
    id          CHAR(36)        NOT NULL,
    user_id     CHAR(36)        NOT NULL,
    tenant_id   CHAR(36)        NOT NULL,
    balance     DECIMAL(10,2)   NOT NULL DEFAULT 10.00,
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_credit_wallets            PRIMARY KEY (id),
    CONSTRAINT uq_credit_wallets_user       UNIQUE (user_id),
    CONSTRAINT fk_wallet_user               FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE CASCADE,
    CONSTRAINT fk_wallet_tenant             FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT,
    CONSTRAINT chk_wallet_balance           CHECK (balance >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── credit_transactions ───────────────────────────────────────
CREATE TABLE credit_transactions (
    id              CHAR(36)        NOT NULL,
    tenant_id       CHAR(36)        NOT NULL,
    user_id         CHAR(36)        NOT NULL,
    amount          DECIMAL(10,2)   NOT NULL,
    type            ENUM('EARN','SPEND','REFUND','PENALTY','ADJUSTMENT') NOT NULL,
    reference_type  VARCHAR(100),
    reference_id    CHAR(36),
    description     VARCHAR(500),
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_credit_transactions       PRIMARY KEY (id),
    CONSTRAINT fk_credit_tx_user            FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE RESTRICT,
    CONSTRAINT fk_credit_tx_tenant          FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── reviews ───────────────────────────────────────────────────
CREATE TABLE reviews (
    id              CHAR(36)        NOT NULL,
    session_id      CHAR(36)        NOT NULL,
    tenant_id       CHAR(36)        NOT NULL,
    reviewer_id     CHAR(36)        NOT NULL,
    reviewee_id     CHAR(36)        NOT NULL,
    rating          TINYINT         NOT NULL,
    comment         TEXT,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_reviews                   PRIMARY KEY (id),
    CONSTRAINT uq_reviews_session_reviewer  UNIQUE (session_id, reviewer_id),
    CONSTRAINT fk_reviews_session           FOREIGN KEY (session_id)  REFERENCES sessions(id)  ON DELETE RESTRICT,
    CONSTRAINT fk_reviews_reviewer          FOREIGN KEY (reviewer_id) REFERENCES users(id)     ON DELETE RESTRICT,
    CONSTRAINT fk_reviews_reviewee          FOREIGN KEY (reviewee_id) REFERENCES users(id)     ON DELETE RESTRICT,
    CONSTRAINT fk_reviews_tenant            FOREIGN KEY (tenant_id)   REFERENCES tenants(id)   ON DELETE RESTRICT,
    CONSTRAINT chk_reviews_rating           CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_reviews_self             CHECK (reviewer_id <> reviewee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── trust_scores ──────────────────────────────────────────────
CREATE TABLE trust_scores (
    id                  CHAR(36)        NOT NULL,
    user_id             CHAR(36)        NOT NULL,
    tenant_id           CHAR(36)        NOT NULL,
    score               DECIMAL(5,2)    NOT NULL DEFAULT 100.00,
    rating_score        DECIMAL(5,2)    NOT NULL DEFAULT 100.00,
    completion_score    DECIMAL(5,2)    NOT NULL DEFAULT 100.00,
    reliability_score   DECIMAL(5,2)    NOT NULL DEFAULT 100.00,
    response_score      DECIMAL(5,2)    NOT NULL DEFAULT 100.00,
    cancellation_score  DECIMAL(5,2)    NOT NULL DEFAULT 100.00,
    calculated_at       DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_trust_scores          PRIMARY KEY (id),
    CONSTRAINT uq_trust_scores_user     UNIQUE (user_id),
    CONSTRAINT fk_trust_user            FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE CASCADE,
    CONSTRAINT fk_trust_tenant          FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── notifications ─────────────────────────────────────────────
CREATE TABLE notifications (
    id          CHAR(36)        NOT NULL,
    user_id     CHAR(36)        NOT NULL,
    tenant_id   CHAR(36)        NOT NULL,
    type        VARCHAR(100)    NOT NULL,
    title       VARCHAR(300)    NOT NULL,
    message     TEXT            NOT NULL,
    read_at     DATETIME(6),
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_notifications         PRIMARY KEY (id),
    CONSTRAINT fk_notifications_user    FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE CASCADE,
    CONSTRAINT fk_notifications_tenant  FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── disputes ──────────────────────────────────────────────────
CREATE TABLE disputes (
    id              CHAR(36)        NOT NULL,
    session_id      CHAR(36)        NOT NULL,
    tenant_id       CHAR(36)        NOT NULL,
    raised_by       CHAR(36)        NOT NULL,
    reason          VARCHAR(300)    NOT NULL,
    description     TEXT,
    status          ENUM('OPEN','UNDER_REVIEW','RESOLVED','REJECTED') NOT NULL DEFAULT 'OPEN',
    resolution      TEXT,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    resolved_at     DATETIME(6),

    CONSTRAINT pk_disputes              PRIMARY KEY (id),
    CONSTRAINT fk_disputes_session      FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE RESTRICT,
    CONSTRAINT fk_disputes_raised_by    FOREIGN KEY (raised_by)  REFERENCES users(id)    ON DELETE RESTRICT,
    CONSTRAINT fk_disputes_tenant       FOREIGN KEY (tenant_id)  REFERENCES tenants(id)  ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Indexes ───────────────────────────────────────────────────
CREATE INDEX idx_availability_user              ON user_availability(user_id);
CREATE INDEX idx_availability_user_day          ON user_availability(user_id, day_of_week);
CREATE INDEX idx_availability_tenant            ON user_availability(tenant_id);

CREATE INDEX idx_sessions_teacher_start         ON sessions(teacher_id, scheduled_start);
CREATE INDEX idx_sessions_learner_start         ON sessions(learner_id, scheduled_start);
CREATE INDEX idx_sessions_status                ON sessions(status);
CREATE INDEX idx_sessions_tenant                ON sessions(tenant_id);
CREATE INDEX idx_sessions_exchange_req          ON sessions(exchange_request_id);

CREATE INDEX idx_credit_wallets_user            ON credit_wallets(user_id);
CREATE INDEX idx_credit_wallets_tenant          ON credit_wallets(tenant_id);

CREATE INDEX idx_credit_tx_user_created         ON credit_transactions(user_id, created_at);
CREATE INDEX idx_credit_tx_tenant               ON credit_transactions(tenant_id);
CREATE INDEX idx_credit_tx_reference            ON credit_transactions(reference_type, reference_id);

CREATE INDEX idx_reviews_reviewee               ON reviews(reviewee_id);
CREATE INDEX idx_reviews_session                ON reviews(session_id);
CREATE INDEX idx_reviews_tenant                 ON reviews(tenant_id);

CREATE INDEX idx_trust_scores_user              ON trust_scores(user_id);
CREATE INDEX idx_trust_scores_tenant            ON trust_scores(tenant_id);

CREATE INDEX idx_notifications_user_read        ON notifications(user_id, read_at);
CREATE INDEX idx_notifications_tenant           ON notifications(tenant_id);

CREATE INDEX idx_disputes_session               ON disputes(session_id);
CREATE INDEX idx_disputes_tenant                ON disputes(tenant_id);
CREATE INDEX idx_disputes_raised_by             ON disputes(raised_by);
